package kz.dissertation.caranalytics.controller;

import jakarta.validation.Valid;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;
import kz.dissertation.caranalytics.dto.WifiObdScanRequest;
import kz.dissertation.caranalytics.service.ObdLiveScanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/live-scan")
public class ObdLiveScanController {

    private final ObdLiveScanService obdLiveScanService;

    public ObdLiveScanController(ObdLiveScanService obdLiveScanService) {
        this.obdLiveScanService = obdLiveScanService;
    }

    @PostMapping("/wifi")
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticSessionResponse scanViaWifi(@Valid @RequestBody WifiObdScanRequest request) {
        return obdLiveScanService.scanViaWifi(request);
    }
}
