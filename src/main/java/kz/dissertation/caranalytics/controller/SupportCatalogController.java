package kz.dissertation.caranalytics.controller;

import java.util.List;
import kz.dissertation.caranalytics.dto.FaultCodeDictionaryResponse;
import kz.dissertation.caranalytics.dto.RepairGuideResponse;
import kz.dissertation.caranalytics.dto.ServiceCenterResponse;
import kz.dissertation.caranalytics.dto.SparePartResponse;
import kz.dissertation.caranalytics.service.FaultCodeDictionaryService;
import kz.dissertation.caranalytics.service.SupportCatalogService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class SupportCatalogController {

    private final SupportCatalogService supportCatalogService;
    private final FaultCodeDictionaryService faultCodeDictionaryService;

    public SupportCatalogController(
            SupportCatalogService supportCatalogService,
            FaultCodeDictionaryService faultCodeDictionaryService
    ) {
        this.supportCatalogService = supportCatalogService;
        this.faultCodeDictionaryService = faultCodeDictionaryService;
    }

    @GetMapping("/service-centers")
    public List<ServiceCenterResponse> getServiceCenters(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Boolean emergencyOnly,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double maxDistanceKm
    ) {
        return supportCatalogService.findServiceCenters(
                specialization,
                emergencyOnly,
                city,
                latitude,
                longitude,
                maxDistanceKm
        );
    }

    @GetMapping("/spare-parts")
    public List<SparePartResponse> getSpareParts(
            @RequestParam(required = false) String faultCode,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        return supportCatalogService.findSpareParts(faultCode, brand, model);
    }

    @GetMapping("/repair-guides")
    public List<RepairGuideResponse> getRepairGuides(@RequestParam(required = false) String faultCode) {
        return supportCatalogService.findRepairGuides(faultCode);
    }

    @GetMapping("/fault-codes")
    public List<FaultCodeDictionaryResponse> getFaultCodes(
            @RequestParam(required = false) String code,
            @RequestParam(name = "system", required = false) String systemName,
            @RequestParam(required = false) String subsystem
    ) {
        return faultCodeDictionaryService.findEntries(code, systemName, subsystem);
    }

    @GetMapping("/fault-codes/{code}")
    public FaultCodeDictionaryResponse getFaultCodeByCode(@PathVariable String code) {
        return faultCodeDictionaryService.findByCode(code);
    }
}
