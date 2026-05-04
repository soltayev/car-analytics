package kz.dissertation.caranalytics.service;

import java.util.List;
import kz.dissertation.caranalytics.model.FaultCodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Elm327WifiScannerTest {

    private final Elm327WifiScanner scanner = new Elm327WifiScanner();

    @Test
    void decodeCurrentPidReading() {
        Elm327WifiScanner.CommandResponse response = scanner.normalizeResponse("010C", "010C\r41 0C 1A F8\r>");

        Elm327WifiScanner.DecodedReading reading = scanner.decodeReading(response, "01", "0C", false);

        assertNotNull(reading);
        assertEquals("rpm", reading.parameterName());
        assertEquals("0C", reading.pidCode());
        assertEquals("1726.00", reading.parameterValue().toPlainString());
    }

    @Test
    void decodeStoredFaultCodes() {
        Elm327WifiScanner.CommandResponse response = scanner.normalizeResponse("03", "03\r43 01 18 03 01 00 00\r>");

        var faultCodes = scanner.decodeFaultCodes(response, "03", FaultCodeType.STORED);

        assertEquals(2, faultCodes.size());
        assertEquals("P0118", faultCodes.get(0).getCode());
        assertEquals("P0301", faultCodes.get(1).getCode());
    }

    @Test
    void decodeVehicleVin() {
        Elm327WifiScanner.CommandResponse response = scanner.normalizeResponse(
                "0902",
                "0902\r49 02 01 57 41 55 5A 5A 5A 38\r49 02 02 56 37 4A 41 31 32 33\r49 02 03 34 35 36\r>"
        );

        String vin = scanner.decodeAsciiVehicleInfo(response, "49", "02");

        assertEquals("WAUZZZ8V7JA123456", vin);
    }

    @Test
    void normalizeResponseDropsNoiseLines() {
        Elm327WifiScanner.CommandResponse response = scanner.normalizeResponse(
                "0105",
                "SEARCHING...\r0105\r41 05 94\r>"
        );

        assertFalse(response.lines().isEmpty());
        assertEquals(List.of("41 05 94"), response.lines());
    }
}
