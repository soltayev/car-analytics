package kz.dissertation.caranalytics;

import java.math.BigDecimal;
import java.util.List;
import kz.dissertation.caranalytics.dto.DiagnosticSessionRequest;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;
import kz.dissertation.caranalytics.dto.FaultCodeDictionaryResponse;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;
import kz.dissertation.caranalytics.dto.ObdReadingRequest;
import kz.dissertation.caranalytics.dto.RawObdFrameRequest;
import kz.dissertation.caranalytics.model.ConnectionType;
import kz.dissertation.caranalytics.model.ObdProtocol;
import kz.dissertation.caranalytics.model.ReportUrgency;
import kz.dissertation.caranalytics.model.SeverityLevel;
import kz.dissertation.caranalytics.service.DiagnosticSessionService;
import kz.dissertation.caranalytics.service.FaultCodeDictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class FaultCodeDictionaryIntegrationTest {

    @Autowired
    private FaultCodeDictionaryService faultCodeDictionaryService;

    @Autowired
    private DiagnosticSessionService diagnosticSessionService;

    @Test
    void findKnownFaultCodeInDictionary() {
        FaultCodeDictionaryResponse entry = faultCodeDictionaryService.findByCode("P0118");

        assertEquals("P0118", entry.code());
        assertEquals("Cooling", entry.subsystem());
        assertEquals(SeverityLevel.CRITICAL, entry.defaultSeverity());
        assertFalse(entry.drivableAllowed());
    }

    @Test
    void createSessionEnrichesFaultCodeFromDictionary() {
        DiagnosticSessionResponse response = diagnosticSessionService.create(buildSessionRequest());

        assertEquals(1, response.faultCodes().size());
        assertEquals("P0118", response.faultCodes().get(0).code());
        assertEquals("Engine coolant temperature circuit high input", response.faultCodes().get(0).description());
        assertEquals(SeverityLevel.CRITICAL, response.faultCodes().get(0).severity());
        assertNotNull(response.faultCodes().get(0).dictionaryEntry());
        assertEquals("Cooling", response.faultCodes().get(0).dictionaryEntry().subsystem());
        assertEquals(ReportUrgency.IMMEDIATE_STOP, response.report().urgency());
    }

    private DiagnosticSessionRequest buildSessionRequest() {
        DiagnosticSessionRequest request = new DiagnosticSessionRequest();
        request.setVehicleId(1L);
        request.setConnectionType(ConnectionType.WIFI);
        request.setAdapterName("ELM327 WiFi");
        request.setAdapterIdentifier("192.168.0.10:35000");
        request.setProtocol(ObdProtocol.ISO_15765_4_CAN);
        request.setRawFrames(List.of(rawFrame()));
        request.setReadings(List.of(engineTemperature()));
        request.setFaultCodes(List.of(faultCode("P0118")));
        return request;
    }

    private RawObdFrameRequest rawFrame() {
        RawObdFrameRequest request = new RawObdFrameRequest();
        request.setMode("03");
        request.setRawResponse("43 01 18");
        request.setDecodedLabel("Stored Diagnostic Trouble Codes");
        return request;
    }

    private ObdReadingRequest engineTemperature() {
        ObdReadingRequest request = new ObdReadingRequest();
        request.setParameterName("engine_temperature");
        request.setPidCode("05");
        request.setParameterValue(new BigDecimal("108"));
        request.setUnit("C");
        return request;
    }

    private FaultCodeRequest faultCode(String code) {
        FaultCodeRequest request = new FaultCodeRequest();
        request.setCode(code);
        return request;
    }
}
