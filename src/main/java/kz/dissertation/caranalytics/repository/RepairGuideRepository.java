package kz.dissertation.caranalytics.repository;

import kz.dissertation.caranalytics.model.RepairGuide;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairGuideRepository extends JpaRepository<RepairGuide, Long> {
}
