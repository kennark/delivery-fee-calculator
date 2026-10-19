package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.WeatherConditionFee;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.data.enums.WeatherConditionRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherConditionFeeRepository extends JpaRepository<WeatherConditionFee, Long> {

    @Query("""
            select w from WeatherConditionFee w
            where w.type = ?1 and w.vehicleType = ?2 and
                        (w.minValue is null or w.minValue <= ?3) and
                        (w.maxValue is null or w.maxValue > ?3)""")
    Optional<WeatherConditionFee> findByTypeAndVehicleTypeAndNumericValue(WeatherConditionRuleType type, VehicleType vehicleType, Double value);

    @Query("""
            select w from WeatherConditionFee w
            where w.type = ?1 and w.vehicleType = ?2 and upper(?3) like upper(concat('%', w.stringValue, '%'))""")
    Optional<WeatherConditionFee> findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(WeatherConditionRuleType type, VehicleType vehicleType, String stringValue);

}
