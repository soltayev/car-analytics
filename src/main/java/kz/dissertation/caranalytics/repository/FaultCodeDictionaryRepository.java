package kz.dissertation.caranalytics.repository;

import java.util.Optional;
import kz.dissertation.caranalytics.model.FaultCodeDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaultCodeDictionaryRepository extends JpaRepository<FaultCodeDictionary, Long> {

    Optional<FaultCodeDictionary> findByCodeIgnoreCase(String code);
}
