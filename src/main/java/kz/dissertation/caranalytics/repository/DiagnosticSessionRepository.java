package kz.dissertation.caranalytics.repository;

import kz.dissertation.caranalytics.model.DiagnosticSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticSessionRepository extends JpaRepository<DiagnosticSession, Long> {
}
