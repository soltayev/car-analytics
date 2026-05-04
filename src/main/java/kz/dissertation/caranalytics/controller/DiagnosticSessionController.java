package kz.dissertation.caranalytics.controller;

import jakarta.validation.Valid;
import java.util.List;
import kz.dissertation.caranalytics.dto.DiagnosticReportResponse;
import kz.dissertation.caranalytics.dto.DiagnosticSessionRequest;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;
import kz.dissertation.caranalytics.service.DiagnosticSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnostic-sessions")
public class DiagnosticSessionController {

    private final DiagnosticSessionService diagnosticSessionService;

    public DiagnosticSessionController(DiagnosticSessionService diagnosticSessionService) {
        this.diagnosticSessionService = diagnosticSessionService;
    }

    @PostMapping({"", "/ingest"})
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticSessionResponse create(@Valid @RequestBody DiagnosticSessionRequest request) {
        return diagnosticSessionService.create(request);
    }

    @GetMapping
    public List<DiagnosticSessionResponse> findAll() {
        return diagnosticSessionService.findAll();
    }

    @GetMapping("/{id}")
    public DiagnosticSessionResponse findById(@PathVariable Long id) {
        return diagnosticSessionService.findById(id);
    }

    @GetMapping("/{id}/report")
    public DiagnosticReportResponse getReport(@PathVariable Long id) {
        return diagnosticSessionService.getReport(id);
    }
}
