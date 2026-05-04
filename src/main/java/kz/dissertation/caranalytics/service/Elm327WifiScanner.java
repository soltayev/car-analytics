package kz.dissertation.caranalytics.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;
import kz.dissertation.caranalytics.dto.ObdReadingRequest;
import kz.dissertation.caranalytics.dto.RawObdFrameRequest;
import kz.dissertation.caranalytics.dto.VehicleInfoItemRequest;
import kz.dissertation.caranalytics.dto.WifiObdScanRequest;
import kz.dissertation.caranalytics.exception.ObdConnectionException;
import kz.dissertation.caranalytics.model.FaultCodeType;
import org.springframework.stereotype.Service;

@Service
public class Elm327WifiScanner {

    private static final List<String> DEFAULT_CURRENT_PIDS = List.of("05", "0C", "0D", "11", "2F", "42");
    private static final List<String> DEFAULT_FREEZE_FRAME_PIDS = List.of("05", "0C", "0D", "11");
    private static final Map<String, String> PID_LABELS = Map.of(
            "05", "engine_temperature",
            "0C", "rpm",
            "0D", "vehicle_speed",
            "11", "throttle_position",
            "2F", "fuel_level",
            "42", "control_module_voltage"
    );
    private static final Map<String, String> PID_UNITS = Map.of(
            "05", "C",
            "0C", "rpm",
            "0D", "km/h",
            "11", "%",
            "2F", "%",
            "42", "V"
    );
    public LiveScanPayload scan(WifiObdScanRequest request) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(request.getHost(), request.getPort()), timeoutMs(request));
            socket.setSoTimeout(timeoutMs(request));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

                initializeAdapter(writer, reader);

                List<RawObdFrameRequest> rawFrames = new ArrayList<>();
                List<VehicleInfoItemRequest> vehicleInfoItems = new ArrayList<>();
                List<ObdReadingRequest> readings = new ArrayList<>();
                List<FaultCodeRequest> faultCodes = new ArrayList<>();

                scanCurrentData(writer, reader, rawFrames, readings, request);
                if (shouldIncludeFreezeFrame(request)) {
                    scanFreezeFrame(writer, reader, rawFrames, readings, request);
                }
                if (shouldIncludeStoredFaultCodes(request)) {
                    scanFaultCodes(writer, reader, rawFrames, faultCodes, "03", FaultCodeType.STORED);
                }
                if (shouldIncludePendingFaultCodes(request)) {
                    scanFaultCodes(writer, reader, rawFrames, faultCodes, "07", FaultCodeType.PENDING);
                }
                if (shouldIncludeVehicleInfo(request)) {
                    scanVehicleInfo(writer, reader, rawFrames, vehicleInfoItems);
                }

                if (rawFrames.isEmpty()) {
                    throw new ObdConnectionException("ELM327 adapter returned no OBD data");
                }

                return new LiveScanPayload(rawFrames, vehicleInfoItems, readings, faultCodes);
            }
        } catch (IOException exception) {
            throw new ObdConnectionException(
                    "Failed to connect to OBD2 adapter at " + request.getHost() + ":" + request.getPort(),
                    exception
            );
        }
    }

    private void initializeAdapter(BufferedWriter writer, BufferedReader reader) throws IOException {
        sendCommand(writer, reader, "ATZ");
        sendCommand(writer, reader, "ATE0");
        sendCommand(writer, reader, "ATL0");
        sendCommand(writer, reader, "ATS0");
        sendCommand(writer, reader, "ATH0");
        sendCommand(writer, reader, "ATSP0");
    }

    private void scanCurrentData(
            BufferedWriter writer,
            BufferedReader reader,
            List<RawObdFrameRequest> rawFrames,
            List<ObdReadingRequest> readings,
            WifiObdScanRequest request
    ) throws IOException {
        for (String pid : currentDataPids(request)) {
            CommandResponse response = sendCommand(writer, reader, "01" + pid);
            rawFrames.add(toRawFrame("01", pid, response));
            DecodedReading reading = decodeReading(response, "01", pid, false);
            if (reading != null) {
                readings.add(toReadingRequest(reading, false));
            }
        }
    }

    private void scanFreezeFrame(
            BufferedWriter writer,
            BufferedReader reader,
            List<RawObdFrameRequest> rawFrames,
            List<ObdReadingRequest> readings,
            WifiObdScanRequest request
    ) throws IOException {
        for (String pid : freezeFramePids(request)) {
            CommandResponse response = sendCommand(writer, reader, "02" + pid);
            rawFrames.add(toRawFrame("02", pid, response));
            DecodedReading reading = decodeReading(response, "02", pid, true);
            if (reading != null) {
                readings.add(toReadingRequest(reading, true));
            }
        }
    }

    private void scanFaultCodes(
            BufferedWriter writer,
            BufferedReader reader,
            List<RawObdFrameRequest> rawFrames,
            List<FaultCodeRequest> faultCodes,
            String mode,
            FaultCodeType faultCodeType
    ) throws IOException {
        CommandResponse response = sendCommand(writer, reader, mode);
        rawFrames.add(toRawFrame(mode, null, response));
        faultCodes.addAll(decodeFaultCodes(response, mode, faultCodeType));
    }

    private void scanVehicleInfo(
            BufferedWriter writer,
            BufferedReader reader,
            List<RawObdFrameRequest> rawFrames,
            List<VehicleInfoItemRequest> vehicleInfoItems
    ) throws IOException {
        CommandResponse vinResponse = sendCommand(writer, reader, "0902");
        rawFrames.add(toRawFrame("09", "02", vinResponse));
        String vin = decodeAsciiVehicleInfo(vinResponse, "49", "02");
        if (vin != null && !vin.isBlank()) {
            VehicleInfoItemRequest item = new VehicleInfoItemRequest();
            item.setInfoKey("VIN");
            item.setInfoValue(vin);
            item.setSourceMode("09");
            vehicleInfoItems.add(item);
        }
    }

    private CommandResponse sendCommand(
            BufferedWriter writer,
            BufferedReader reader,
            String command
    ) throws IOException {
        writer.write(command);
        writer.write("\r");
        writer.flush();

        StringBuilder payload = new StringBuilder();
        int currentChar;
        while ((currentChar = reader.read()) != -1) {
            if (currentChar == '>') {
                break;
            }
            payload.append((char) currentChar);
        }

        return normalizeResponse(command, payload.toString());
    }

    CommandResponse normalizeResponse(String command, String payload) {
        List<String> lines = Arrays.stream(payload.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> stripCommandEcho(line, command))
                .filter(line -> !line.startsWith("SEARCHING"))
                .filter(line -> !line.equalsIgnoreCase("OK"))
                .filter(line -> !line.equalsIgnoreCase("NO DATA"))
                .map(this::normalizeHexLine)
                .filter(line -> !line.isBlank())
                .filter(this::containsHexByte)
                .toList();

        return new CommandResponse(
                command,
                payload,
                lines,
                lines.stream().flatMap(line -> Arrays.stream(line.split(" "))).toList()
        );
    }

    private String stripCommandEcho(String line, String command) {
        String normalized = line.replaceAll("\\s+", "").toUpperCase();
        String normalizedCommand = command.replaceAll("\\s+", "").toUpperCase();
        if (normalized.startsWith(normalizedCommand)) {
            String remainder = normalized.substring(normalizedCommand.length());
            return insertSpaces(remainder);
        }
        return line;
    }

    private String normalizeHexLine(String value) {
        String hexOnly = value.toUpperCase()
                .replaceAll("[^0-9A-F ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return hexOnly;
    }

    private boolean containsHexByte(String value) {
        return value.matches(".*\\b[0-9A-F]{2}\\b.*");
    }

    private String insertSpaces(String value) {
        if (value.isBlank()) {
            return "";
        }
        List<String> bytes = new ArrayList<>();
        for (int index = 0; index < value.length(); index += 2) {
            int endIndex = Math.min(index + 2, value.length());
            bytes.add(value.substring(index, endIndex));
        }
        return String.join(" ", bytes);
    }

    DecodedReading decodeReading(CommandResponse response, String mode, String pid, boolean freezeFrame) {
        String responseService = "01".equals(mode) ? "41" : "42";
        List<String> tokens = response.tokens();
        for (int index = 0; index < tokens.size() - 1; index++) {
            if (tokens.get(index).equalsIgnoreCase(responseService)
                    && tokens.get(index + 1).equalsIgnoreCase(pid)) {
                List<Integer> bytes = tokens.subList(index + 2, tokens.size()).stream()
                        .map(token -> Integer.parseInt(token, 16))
                        .toList();
                if (bytes.isEmpty()) {
                    return null;
                }
                return switch (pid.toUpperCase()) {
                    case "05" -> new DecodedReading(
                            PID_LABELS.get(pid),
                            pid,
                            BigDecimal.valueOf(bytes.get(0) - 40),
                            PID_UNITS.get(pid),
                            "Engine coolant temperature",
                            mode,
                            false,
                            freezeFrame
                    );
                    case "0C" -> bytes.size() < 2 ? null : new DecodedReading(
                            PID_LABELS.get(pid),
                            pid,
                            BigDecimal.valueOf(((bytes.get(0) * 256) + bytes.get(1)) / 4.0)
                                    .setScale(2, RoundingMode.HALF_UP),
                            PID_UNITS.get(pid),
                            "Engine speed",
                            mode,
                            false,
                            freezeFrame
                    );
                    case "0D" -> new DecodedReading(
                            PID_LABELS.get(pid),
                            pid,
                            BigDecimal.valueOf(bytes.get(0)),
                            PID_UNITS.get(pid),
                            "Vehicle speed",
                            mode,
                            false,
                            freezeFrame
                    );
                    case "11" -> new DecodedReading(
                            PID_LABELS.get(pid),
                            pid,
                            BigDecimal.valueOf(bytes.get(0) * 100.0 / 255.0).setScale(2, RoundingMode.HALF_UP),
                            PID_UNITS.get(pid),
                            "Throttle position",
                            mode,
                            false,
                            freezeFrame
                    );
                    case "2F" -> new DecodedReading(
                            PID_LABELS.get(pid),
                            pid,
                            BigDecimal.valueOf(bytes.get(0) * 100.0 / 255.0).setScale(2, RoundingMode.HALF_UP),
                            PID_UNITS.get(pid),
                            "Fuel level input",
                            mode,
                            false,
                            freezeFrame
                    );
                    case "42" -> bytes.size() < 2 ? null : new DecodedReading(
                            PID_LABELS.get(pid),
                            pid,
                            BigDecimal.valueOf(((bytes.get(0) * 256) + bytes.get(1)) / 1000.0)
                                    .setScale(3, RoundingMode.HALF_UP),
                            PID_UNITS.get(pid),
                            "Control module voltage",
                            mode,
                            false,
                            freezeFrame
                    );
                    default -> null;
                };
            }
        }
        return null;
    }

    List<FaultCodeRequest> decodeFaultCodes(
            CommandResponse response,
            String mode,
            FaultCodeType faultCodeType
    ) {
        String responseService = "03".equals(mode) ? "43" : "47";
        List<String> tokens = response.tokens();
        int serviceIndex = tokens.indexOf(responseService);
        if (serviceIndex < 0) {
            return List.of();
        }

        List<FaultCodeRequest> decodedFaultCodes = new ArrayList<>();
        for (int index = serviceIndex + 1; index + 1 < tokens.size(); index += 2) {
            int firstByte = Integer.parseInt(tokens.get(index), 16);
            int secondByte = Integer.parseInt(tokens.get(index + 1), 16);
            if (firstByte == 0 && secondByte == 0) {
                continue;
            }

            String code = decodeDtc(firstByte, secondByte);
            FaultCodeRequest request = new FaultCodeRequest();
            request.setCode(code);
            request.setDescription(null);
            request.setFaultCodeType(faultCodeType);
            request.setSourceMode(mode);
            request.setManufacturerSpecific(isManufacturerSpecificCode(code));
            request.setSeverity(null);
            decodedFaultCodes.add(request);
        }

        return decodedFaultCodes;
    }

    String decodeAsciiVehicleInfo(CommandResponse response, String headerMode, String pid) {
        List<Integer> bytes = new ArrayList<>();
        for (String line : response.lines()) {
            List<String> tokens = Arrays.stream(line.split(" "))
                    .filter(token -> !token.isBlank())
                    .toList();
            if (tokens.size() >= 4
                    && tokens.get(0).equalsIgnoreCase(headerMode)
                    && tokens.get(1).equalsIgnoreCase(pid)) {
                for (int index = 3; index < tokens.size(); index++) {
                    bytes.add(Integer.parseInt(tokens.get(index), 16));
                }
            }
        }

        String value = bytes.stream()
                .filter(byteValue -> byteValue > 0)
                .map(byteValue -> String.valueOf((char) byteValue.intValue()))
                .collect(Collectors.joining())
                .trim();

        return value.isBlank() ? null : value;
    }

    private RawObdFrameRequest toRawFrame(String mode, String pid, CommandResponse response) {
        RawObdFrameRequest request = new RawObdFrameRequest();
        request.setMode(mode);
        request.setPid(pid);
        request.setRawResponse(response.lines().isEmpty() ? normalizeHexLine(response.payload()) : String.join(" | ", response.lines()));
        request.setDecodedLabel(resolveDecodedLabel(mode, pid));
        request.setManufacturerSpecific(isManufacturerSpecific(mode));
        request.setFrameTimestamp(LocalDateTime.now());
        return request;
    }

    private ObdReadingRequest toReadingRequest(DecodedReading reading, boolean freezeFrame) {
        ObdReadingRequest request = new ObdReadingRequest();
        request.setParameterName(reading.parameterName());
        request.setPidCode(reading.pidCode());
        request.setSourceMode(reading.sourceMode());
        request.setFreezeFrame(freezeFrame);
        request.setManufacturerSpecific(reading.manufacturerSpecific());
        request.setDescription(reading.description());
        request.setParameterValue(reading.parameterValue());
        request.setUnit(reading.unit());
        return request;
    }

    private String decodeDtc(int firstByte, int secondByte) {
        String family = switch ((firstByte & 0xC0) >> 6) {
            case 0 -> "P";
            case 1 -> "C";
            case 2 -> "B";
            default -> "U";
        };

        int secondCharacter = (firstByte & 0x30) >> 4;
        int thirdCharacter = firstByte & 0x0F;
        int fourthCharacter = (secondByte & 0xF0) >> 4;
        int fifthCharacter = secondByte & 0x0F;

        return family + secondCharacter
                + Integer.toHexString(thirdCharacter).toUpperCase()
                + Integer.toHexString(fourthCharacter).toUpperCase()
                + Integer.toHexString(fifthCharacter).toUpperCase();
    }

    private boolean isManufacturerSpecificCode(String code) {
        return code != null && code.length() >= 2 && code.charAt(1) == '1';
    }

    private String resolveDecodedLabel(String mode, String pid) {
        if ("09".equals(mode) && "02".equals(pid)) {
            return "Vehicle Identification Number";
        }
        if ("03".equals(mode)) {
            return "Stored Diagnostic Trouble Codes";
        }
        if ("07".equals(mode)) {
            return "Pending Diagnostic Trouble Codes";
        }
        if ("02".equals(mode) && pid != null) {
            return "Freeze Frame " + PID_LABELS.getOrDefault(pid.toUpperCase(), pid);
        }
        return PID_LABELS.getOrDefault(Objects.toString(pid, mode), "OBD Command " + mode + (pid == null ? "" : " " + pid));
    }

    private boolean isManufacturerSpecific(String mode) {
        return "21".equals(mode) || "22".equals(mode);
    }

    private List<String> currentDataPids(WifiObdScanRequest request) {
        return request.getCurrentDataPids() == null || request.getCurrentDataPids().isEmpty()
                ? DEFAULT_CURRENT_PIDS
                : request.getCurrentDataPids().stream().map(String::toUpperCase).toList();
    }

    private List<String> freezeFramePids(WifiObdScanRequest request) {
        return request.getFreezeFramePids() == null || request.getFreezeFramePids().isEmpty()
                ? DEFAULT_FREEZE_FRAME_PIDS
                : request.getFreezeFramePids().stream().map(String::toUpperCase).toList();
    }

    private boolean shouldIncludeStoredFaultCodes(WifiObdScanRequest request) {
        return request.getIncludeStoredFaultCodes() == null || request.getIncludeStoredFaultCodes();
    }

    private boolean shouldIncludePendingFaultCodes(WifiObdScanRequest request) {
        return request.getIncludePendingFaultCodes() == null || request.getIncludePendingFaultCodes();
    }

    private boolean shouldIncludeFreezeFrame(WifiObdScanRequest request) {
        return request.getIncludeFreezeFrame() == null || request.getIncludeFreezeFrame();
    }

    private boolean shouldIncludeVehicleInfo(WifiObdScanRequest request) {
        return request.getIncludeVehicleInfo() == null || request.getIncludeVehicleInfo();
    }

    private int timeoutMs(WifiObdScanRequest request) {
        return request.getSocketTimeoutMs() == null ? 4000 : request.getSocketTimeoutMs();
    }

    record CommandResponse(
            String command,
            String payload,
            List<String> lines,
            List<String> tokens
    ) {
    }

    record DecodedReading(
            String parameterName,
            String pidCode,
            BigDecimal parameterValue,
            String unit,
            String description,
            String sourceMode,
            Boolean manufacturerSpecific,
            Boolean freezeFrame
    ) {
    }

    public record LiveScanPayload(
            List<RawObdFrameRequest> rawFrames,
            List<VehicleInfoItemRequest> vehicleInfoItems,
            List<ObdReadingRequest> readings,
            List<FaultCodeRequest> faultCodes
    ) {
    }
}
