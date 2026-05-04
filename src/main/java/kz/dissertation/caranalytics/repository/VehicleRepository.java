package kz.dissertation.caranalytics.repository;

import kz.dissertation.caranalytics.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
