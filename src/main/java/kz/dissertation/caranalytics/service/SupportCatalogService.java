package kz.dissertation.caranalytics.service;

import java.util.List;
import kz.dissertation.caranalytics.dto.RepairGuideResponse;
import kz.dissertation.caranalytics.dto.ServiceCenterResponse;
import kz.dissertation.caranalytics.dto.SparePartResponse;
import kz.dissertation.caranalytics.model.DiagnosticSession;
import kz.dissertation.caranalytics.model.RecommendationType;
import kz.dissertation.caranalytics.model.Vehicle;

public interface SupportCatalogService {

    List<ServiceCenterResponse> findServiceCenters(
            String specialization,
            Boolean emergencyOnly,
            String city,
            Double latitude,
            Double longitude,
            Double maxDistanceKm
    );

    List<SparePartResponse> findSpareParts(String faultCode, String brand, String model);

    List<RepairGuideResponse> findRepairGuides(String faultCode);

    List<ServiceCenterResponse> suggestServiceCenters(
            RecommendationType recommendationType,
            DiagnosticSession session,
            String referenceCode
    );

    List<SparePartResponse> suggestSpareParts(Vehicle vehicle, String referenceCode);

    List<RepairGuideResponse> suggestRepairGuides(String referenceCode);
}
