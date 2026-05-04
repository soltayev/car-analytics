package kz.dissertation.caranalytics.service;

import java.util.List;
import kz.dissertation.caranalytics.dto.VehicleRequest;
import kz.dissertation.caranalytics.dto.VehicleResponse;
import kz.dissertation.caranalytics.exception.ResourceNotFoundException;
import kz.dissertation.caranalytics.model.Vehicle;
import kz.dissertation.caranalytics.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehicleResponse create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(request.getVin());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setProductionYear(request.getProductionYear());
        vehicle.setEngineType(request.getEngineType());
        return map(vehicleRepository.save(vehicle));
    }

    @Override
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    public VehicleResponse findById(Long id) {
        return map(getVehicle(id));
    }

    Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    private VehicleResponse map(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getProductionYear(),
                vehicle.getEngineType()
        );
    }
}
