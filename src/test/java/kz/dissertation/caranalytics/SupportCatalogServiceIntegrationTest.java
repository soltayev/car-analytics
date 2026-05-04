package kz.dissertation.caranalytics;

import java.util.List;
import kz.dissertation.caranalytics.dto.RepairGuideResponse;
import kz.dissertation.caranalytics.dto.ServiceCenterResponse;
import kz.dissertation.caranalytics.dto.SparePartResponse;
import kz.dissertation.caranalytics.service.SupportCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SupportCatalogServiceIntegrationTest {

    @Autowired
    private SupportCatalogService supportCatalogService;

    @Test
    void findRepairArtifactsForFaultCode() {
        List<SparePartResponse> spareParts = supportCatalogService.findSpareParts("P0118", null, null);
        List<RepairGuideResponse> repairGuides = supportCatalogService.findRepairGuides("P0118");
        List<ServiceCenterResponse> emergencyCenters = supportCatalogService.findServiceCenters(
                "Cooling",
                true,
                null,
                null,
                null,
                null
        );

        assertFalse(spareParts.isEmpty());
        assertFalse(repairGuides.isEmpty());
        assertFalse(emergencyCenters.isEmpty());
        assertTrue(emergencyCenters.stream().allMatch(ServiceCenterResponse::emergencySupport));
    }

    @Test
    void findNearestServiceCentersByCoordinates() {
        List<ServiceCenterResponse> serviceCenters = supportCatalogService.findServiceCenters(
                null,
                true,
                "Almaty",
                43.2565,
                76.9284,
                5.0
        );

        assertFalse(serviceCenters.isEmpty());
        assertTrue(serviceCenters.get(0).distanceKm() != null);
        assertTrue(serviceCenters.get(0).distanceKm() <= 5.0);
    }
}
