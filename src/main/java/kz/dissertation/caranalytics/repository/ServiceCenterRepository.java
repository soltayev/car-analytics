package kz.dissertation.caranalytics.repository;

import kz.dissertation.caranalytics.model.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {
}
