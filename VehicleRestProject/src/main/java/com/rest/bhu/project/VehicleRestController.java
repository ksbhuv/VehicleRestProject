package com.rest.bhu.project;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class VehicleRestController {
	
	private final int YEARSTART = 1950;
	private final int YEARNEW = 2050;
   
 	@Autowired
    private VehicleRepository vehicleRepository;

 	//Method to get details of all vehicles
    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getVehicles(
            @RequestParam(value =  "year", required = false) Integer year,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "make", required = false) String make )  {

        List<Vehicle> allVehicles = vehicleRepository.findAll();
        Stream<Vehicle> vehiclesStream = allVehicles.stream();

        if(year==null && model==null && make==null) {
            return ResponseEntity.ok().body(allVehicles);
        }
        if (year != null) {
        	vehiclesStream = vehiclesStream.filter(v -> (v.getYear() == year));
        }
        if (model != null) {
            List<String> models = Arrays.asList(model.split(","));
            vehiclesStream = vehiclesStream.filter(v -> models.contains(v.getModel()));
        }
        if (make != null) {
            List<String> makes = Arrays.asList(make.split(","));
            vehiclesStream = vehiclesStream.filter(v -> makes.contains(v.getMake()));
        }

        return ResponseEntity.ok().body(vehiclesStream.collect(Collectors.toList()));
    }

    //Method to get details of vehicle by id
    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehiclesById(@PathVariable(value="id") int vehicleId)
            throws ResourceNotFoundException {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found on :: " + vehicleId));
        return ResponseEntity.ok().body(vehicle);
    }
    
    //Method to post details of vehicles
    @PostMapping("/vehicles")
    public ResponseEntity createVehicle(@RequestBody Vehicle vehicle) {
        if (vehicle.getId() != 0) {
            vehicle.setId(0);
        }
        if (vehicle.getYear() > YEARNEW || vehicle.getYear() < YEARSTART) {
            return ResponseEntity.badRequest().body("Year out of range. Vehicle was not saved");
        }

        if (vehicle.getMake() == null || vehicle.getMake().length() == 0) {
            return ResponseEntity.badRequest().body("Make was not specified. Vehicle was not saved");
        }

        if (vehicle.getModel() == null || vehicle.getModel().length() == 0) {
            return ResponseEntity.badRequest().body("Model was not specified. Vehicle was not saved");
        }
        vehicleRepository.save(vehicle);
        return ResponseEntity.ok().body(vehicle);
    }

    //Method to update details of vehicles
    @PutMapping("/vehicles")
    public ResponseEntity updateVehicle(@RequestBody Vehicle vehicleDetails) throws ResourceNotFoundException {
        Vehicle vehicle =
                vehicleRepository
                        .findById(vehicleDetails.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found on :: "
                                + vehicleDetails.getId()));

        if (vehicleDetails.getYear() < YEARNEW && vehicleDetails.getYear() > YEARSTART) {
            vehicle.setYear(vehicleDetails.getYear());
        } else {
            return ResponseEntity.badRequest().body("Year out of range. Vehicle was not updated");
        }

        if (vehicleDetails.getMake() != null && vehicleDetails.getMake().length() != 0) {
            vehicle.setMake(vehicleDetails.getMake());
        } else {
            return ResponseEntity.badRequest().body("Make was not specified. Vehicle was not updated");
        }

        if (vehicleDetails.getModel() != null && vehicleDetails.getModel().length() != 0) {
            vehicle.setModel(vehicleDetails.getModel());
        } else {
            return ResponseEntity.badRequest().body("Model was not specified. Vehicle was not updated");
        }
        vehicleRepository.save(vehicle);
        return ResponseEntity.ok().body(vehicle);
    }

    //Method to delete details of vehicle using id
    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<String> deleteVehicle(@PathVariable(value = "id") int vehicleId) throws ResourceNotFoundException {
        Vehicle vehicle =
                vehicleRepository
                        .findById(vehicleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found on :: " + vehicleId));
        vehicleRepository.delete(vehicle);
        return ResponseEntity.ok().body("Vehicle successfully deleted");
    }

}

