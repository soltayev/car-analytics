package kz.dissertation.caranalytics.service;

import java.util.List;
import kz.dissertation.caranalytics.dto.DiagnosticReportResponse;
import kz.dissertation.caranalytics.dto.DiagnosticSessionRequest;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;

public interface DiagnosticSessionService {

    DiagnosticSessionResponse create(DiagnosticSessionRequest request);

    List<DiagnosticSessionResponse> findAll();

    DiagnosticSessionResponse findById(Long id);

    DiagnosticReportResponse getReport(Long id);
}
