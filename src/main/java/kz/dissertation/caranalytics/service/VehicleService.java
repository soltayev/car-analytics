package kz.dissertation.caranalytics.service;

import java.util.List;
import kz.dissertation.caranalytics.dto.VehicleRequest;
import kz.dissertation.caranalytics.dto.VehicleResponse;

public interface VehicleService {

    VehicleResponse create(VehicleRequest request);

    List<VehicleResponse> findAll();

    VehicleResponse findById(Long id);
}
