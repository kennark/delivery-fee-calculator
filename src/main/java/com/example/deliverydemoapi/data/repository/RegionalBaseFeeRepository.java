package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.RegionalBaseFee;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionalBaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {
    Optional<RegionalBaseFee> findByCityAndVehicleType(City city, VehicleType vehicleType);

}
