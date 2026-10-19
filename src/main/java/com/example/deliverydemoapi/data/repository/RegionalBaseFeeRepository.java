package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.RegionalBaseFee;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionalBaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {

    /**
     * Finds the regional base fee rule for the given city and vehicle type.
     *
     * @param city        the city to look up
     * @param vehicleType the vehicle type to look up
     * @return an {@link Optional} containing the matching base fee, or empty if none exists
     */
    Optional<RegionalBaseFee> findByCityAndVehicleType(City city, VehicleType vehicleType);

}
