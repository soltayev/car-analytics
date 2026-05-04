package kz.dissertation.caranalytics.service;

import java.util.Comparator;
import java.util.List;
import kz.dissertation.caranalytics.dto.RepairGuideResponse;
import kz.dissertation.caranalytics.dto.ServiceCenterResponse;
import kz.dissertation.caranalytics.dto.SparePartResponse;
import kz.dissertation.caranalytics.model.DiagnosticSession;
import kz.dissertation.caranalytics.model.RecommendationType;
import kz.dissertation.caranalytics.model.RepairGuide;
import kz.dissertation.caranalytics.model.ReportUrgency;
import kz.dissertation.caranalytics.model.ServiceCenter;
import kz.dissertation.caranalytics.model.SparePart;
import kz.dissertation.caranalytics.model.Vehicle;
import kz.dissertation.caranalytics.repository.RepairGuideRepository;
import kz.dissertation.caranalytics.repository.ServiceCenterRepository;
import kz.dissertation.caranalytics.repository.SparePartRepository;
import org.springframework.stereotype.Service;

@Service
public class SupportCatalogServiceImpl implements SupportCatalogService {

    private final ServiceCenterRepository serviceCenterRepository;
    private final SparePartRepository sparePartRepository;
    private final RepairGuideRepository repairGuideRepository;

    public SupportCatalogServiceImpl(
            ServiceCenterRepository serviceCenterRepository,
            SparePartRepository sparePartRepository,
            RepairGuideRepository repairGuideRepository
    ) {
        this.serviceCenterRepository = serviceCenterRepository;
        this.sparePartRepository = sparePartRepository;
        this.repairGuideRepository = repairGuideRepository;
    }

    @Override
    public List<ServiceCenterResponse> findServiceCenters(
            String specialization,
            Boolean emergencyOnly,
            String city,
            Double latitude,
            Double longitude,
            Double maxDistanceKm
    ) {
        return serviceCenterRepository.findAll().stream()
                .filter(center -> specialization == null
                        || center.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                .filter(center -> emergencyOnly == null || !emergencyOnly || Boolean.TRUE.equals(center.getEmergencySupport()))
                .filter(center -> city == null || matches(center.getCity(), city))
                .map(center -> new RankedServiceCenter(center, resolveDistanceKm(center, latitude, longitude)))
                .filter(rank -> maxDistanceKm == null || rank.distanceKm() == null || rank.distanceKm() <= maxDistanceKm)
                .sorted(serviceCenterComparator(latitude, longitude))
                .map(rank -> mapServiceCenter(rank.center(), rank.distanceKm()))
                .toList();
    }

    @Override
    public List<SparePartResponse> findSpareParts(String faultCode, String brand, String model) {
        return sparePartRepository.findAll().stream()
                .filter(part -> faultCode == null || matches(part.getFaultCode(), faultCode))
                .filter(part -> brand == null || part.getCompatibleBrand() == null || matches(part.getCompatibleBrand(), brand))
                .filter(part -> model == null || part.getCompatibleModel() == null || matches(part.getCompatibleModel(), model))
                .map(this::mapSparePart)
                .toList();
    }

    @Override
    public List<RepairGuideResponse> findRepairGuides(String faultCode) {
        return repairGuideRepository.findAll().stream()
                .filter(guide -> faultCode == null || matches(guide.getFaultCode(), faultCode))
                .map(this::mapRepairGuide)
                .toList();
    }

    @Override
    public List<ServiceCenterResponse> suggestServiceCenters(
            RecommendationType recommendationType,
            DiagnosticSession session,
            String referenceCode
    ) {
        if (recommendationType != RecommendationType.SERVICE_CENTER) {
            return List.of();
        }

        String subsystem = inferSubsystem(referenceCode);
        boolean emergencyOnly = session.getReport() != null
                && (session.getReport().getUrgency() == ReportUrgency.URGENT_SERVICE
                || session.getReport().getUrgency() == ReportUrgency.IMMEDIATE_STOP);

        return serviceCenterRepository.findAll().stream()
                .filter(center -> subsystem == null
                        || center.getSpecialization().toLowerCase().contains(subsystem.toLowerCase())
                        || "General Diagnostics".equalsIgnoreCase(center.getSpecialization()))
                .filter(center -> !emergencyOnly || Boolean.TRUE.equals(center.getEmergencySupport()))
                .sorted(Comparator
                        .comparing(ServiceCenter::getEmergencySupport, Comparator.reverseOrder())
                        .thenComparing(ServiceCenter::getRating, Comparator.reverseOrder()))
                .limit(3)
                .map(center -> mapServiceCenter(center, null))
                .toList();
    }

    @Override
    public List<SparePartResponse> suggestSpareParts(Vehicle vehicle, String referenceCode) {
        if (referenceCode == null || referenceCode.isBlank()) {
            return List.of();
        }

        String subsystem = inferSubsystem(referenceCode);
        return sparePartRepository.findAll().stream()
                .filter(part -> matches(part.getFaultCode(), referenceCode)
                        || (subsystem != null && matches(part.getSubsystem(), subsystem)))
                .filter(part -> part.getCompatibleBrand() == null
                        || matches(part.getCompatibleBrand(), vehicle.getBrand()))
                .filter(part -> part.getCompatibleModel() == null
                        || matches(part.getCompatibleModel(), vehicle.getModel()))
                .limit(3)
                .map(this::mapSparePart)
                .toList();
    }

    @Override
    public List<RepairGuideResponse> suggestRepairGuides(String referenceCode) {
        if (referenceCode == null || referenceCode.isBlank()) {
            return List.of();
        }

        String subsystem = inferSubsystem(referenceCode);
        return repairGuideRepository.findAll().stream()
                .filter(guide -> matches(guide.getFaultCode(), referenceCode)
                        || (subsystem != null && matches(guide.getSubsystem(), subsystem)))
                .limit(3)
                .map(this::mapRepairGuide)
                .toList();
    }

    private ServiceCenterResponse mapServiceCenter(ServiceCenter center, Double distanceKm) {
        return new ServiceCenterResponse(
                center.getId(),
                center.getName(),
                center.getCity(),
                center.getAddress(),
                center.getPhone(),
                center.getSpecialization(),
                center.getEmergencySupport(),
                center.getRating(),
                center.getLatitude(),
                center.getLongitude(),
                distanceKm
        );
    }

    private SparePartResponse mapSparePart(SparePart part) {
        return new SparePartResponse(
                part.getId(),
                part.getPartNumber(),
                part.getPartName(),
                part.getManufacturer(),
                part.getSubsystem(),
                part.getCompatibleBrand(),
                part.getCompatibleModel(),
                part.getFaultCode(),
                part.getPrice(),
                part.getCurrency(),
                part.getStockStatus()
        );
    }

    private RepairGuideResponse mapRepairGuide(RepairGuide guide) {
        return new RepairGuideResponse(
                guide.getId(),
                guide.getTitle(),
                guide.getSubsystem(),
                guide.getFaultCode(),
                guide.getDescription(),
                guide.getDifficulty(),
                guide.getEstimatedMinutes(),
                guide.getSafetyNotes()
        );
    }

    private boolean matches(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private String inferSubsystem(String referenceCode) {
        if (referenceCode == null || referenceCode.isBlank()) {
            return null;
        }
        if (referenceCode.startsWith("P03")) {
            return "Ignition";
        }
        if (referenceCode.startsWith("P01") || "TEMP_OVERHEAT".equalsIgnoreCase(referenceCode)) {
            return "Cooling";
        }
        if (referenceCode.startsWith("P02")) {
            return "Fuel";
        }
        return "General Diagnostics";
    }

    private Double resolveDistanceKm(ServiceCenter center, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(center.getLatitude() - latitude);
        double lonDistance = Math.toRadians(center.getLongitude() - longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude))
                * Math.cos(Math.toRadians(center.getLatitude()))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusKm * c * 100.0) / 100.0;
    }

    private Comparator<RankedServiceCenter> serviceCenterComparator(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return Comparator.comparing(
                            RankedServiceCenter::distanceKm,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
                    .thenComparing(rank -> rank.center().getRating(), Comparator.reverseOrder());
        }

        return Comparator.comparing(
                        (RankedServiceCenter rank) -> rank.center().getEmergencySupport(),
                        Comparator.reverseOrder()
                )
                .thenComparing(rank -> rank.center().getRating(), Comparator.reverseOrder());
    }

    private record RankedServiceCenter(ServiceCenter center, Double distanceKm) {
    }
}
