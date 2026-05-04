package kz.dissertation.caranalytics.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import kz.dissertation.caranalytics.dto.FaultCodeDictionaryResponse;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;
import kz.dissertation.caranalytics.exception.ResourceNotFoundException;
import kz.dissertation.caranalytics.model.FaultCodeDictionary;
import kz.dissertation.caranalytics.model.SeverityLevel;
import kz.dissertation.caranalytics.repository.FaultCodeDictionaryRepository;
import org.springframework.stereotype.Service;

@Service
public class FaultCodeDictionaryServiceImpl implements FaultCodeDictionaryService {

    private static final String UNKNOWN_DTC_DESCRIPTION = "Unknown DTC code. Local dictionary entry is missing.";

    private final FaultCodeDictionaryRepository faultCodeDictionaryRepository;

    public FaultCodeDictionaryServiceImpl(FaultCodeDictionaryRepository faultCodeDictionaryRepository) {
        this.faultCodeDictionaryRepository = faultCodeDictionaryRepository;
    }

    @Override
    public List<FaultCodeRequest> enrichFaultCodes(List<FaultCodeRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        return requests.stream()
                .filter(Objects::nonNull)
                .map(this::enrichFaultCode)
                .toList();
    }

    @Override
    public List<FaultCodeDictionaryResponse> findEntries(String code, String systemName, String subsystem) {
        return faultCodeDictionaryRepository.findAll().stream()
                .filter(entry -> code == null || containsIgnoreCase(entry.getCode(), code))
                .filter(entry -> systemName == null || containsIgnoreCase(entry.getSystemName(), systemName))
                .filter(entry -> subsystem == null || containsIgnoreCase(entry.getSubsystem(), subsystem))
                .map(this::map)
                .toList();
    }

    @Override
    public FaultCodeDictionaryResponse findByCode(String code) {
        return lookupByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("FaultCodeDictionary", code));
    }

    @Override
    public Optional<FaultCodeDictionaryResponse> lookupByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return faultCodeDictionaryRepository.findByCodeIgnoreCase(normalizeCode(code)).map(this::map);
    }

    private FaultCodeRequest enrichFaultCode(FaultCodeRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        Optional<FaultCodeDictionary> dictionaryEntry = faultCodeDictionaryRepository.findByCodeIgnoreCase(normalizedCode);

        FaultCodeRequest enriched = new FaultCodeRequest();
        enriched.setCode(normalizedCode);
        enriched.setDescription(resolveDescription(request, dictionaryEntry));
        enriched.setFaultCodeType(request.getFaultCodeType());
        enriched.setSourceMode(request.getSourceMode());
        enriched.setManufacturerSpecific(resolveManufacturerSpecific(request, dictionaryEntry, normalizedCode));
        enriched.setSeverity(resolveSeverity(request, dictionaryEntry));

        return enriched;
    }

    private String resolveDescription(FaultCodeRequest request, Optional<FaultCodeDictionary> dictionaryEntry) {
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            return request.getDescription();
        }
        return dictionaryEntry.map(FaultCodeDictionary::getTitle).orElse(UNKNOWN_DTC_DESCRIPTION);
    }

    private Boolean resolveManufacturerSpecific(
            FaultCodeRequest request,
            Optional<FaultCodeDictionary> dictionaryEntry,
            String normalizedCode
    ) {
        if (request.getManufacturerSpecific() != null) {
            return request.getManufacturerSpecific();
        }
        return dictionaryEntry.map(FaultCodeDictionary::getManufacturerSpecific)
                .orElse(inferManufacturerSpecific(normalizedCode));
    }

    private SeverityLevel resolveSeverity(
            FaultCodeRequest request,
            Optional<FaultCodeDictionary> dictionaryEntry
    ) {
        if (request.getSeverity() != null) {
            return request.getSeverity();
        }
        return dictionaryEntry.map(FaultCodeDictionary::getDefaultSeverity).orElse(SeverityLevel.MEDIUM);
    }

    private boolean inferManufacturerSpecific(String code) {
        return code.length() >= 2 && code.charAt(1) == '1';
    }

    private FaultCodeDictionaryResponse map(FaultCodeDictionary entry) {
        return new FaultCodeDictionaryResponse(
                entry.getId(),
                entry.getCode(),
                entry.getTitle(),
                entry.getDescription(),
                entry.getSystemName(),
                entry.getSubsystem(),
                entry.getManufacturerSpecific(),
                entry.getDefaultSeverity(),
                entry.getPossibleCauses(),
                entry.getRecommendedActions(),
                entry.getDrivableAllowed()
        );
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String left, String right) {
        return left != null && left.toLowerCase(Locale.ROOT).contains(right.toLowerCase(Locale.ROOT));
    }
}
