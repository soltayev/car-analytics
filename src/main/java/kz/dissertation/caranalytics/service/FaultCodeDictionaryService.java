package kz.dissertation.caranalytics.service;

import java.util.List;
import java.util.Optional;
import kz.dissertation.caranalytics.dto.FaultCodeDictionaryResponse;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;

public interface FaultCodeDictionaryService {

    List<FaultCodeRequest> enrichFaultCodes(List<FaultCodeRequest> requests);

    List<FaultCodeDictionaryResponse> findEntries(String code, String systemName, String subsystem);

    FaultCodeDictionaryResponse findByCode(String code);

    Optional<FaultCodeDictionaryResponse> lookupByCode(String code);
}
