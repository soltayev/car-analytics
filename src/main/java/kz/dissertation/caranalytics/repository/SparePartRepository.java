package kz.dissertation.caranalytics.repository;

import kz.dissertation.caranalytics.model.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
}
