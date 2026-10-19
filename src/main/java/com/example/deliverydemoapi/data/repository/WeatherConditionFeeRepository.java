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

    /**
     * Finds the weather condition fee rule matching the given type, vehicle type and numeric value.
     * A rule matches when its {@code minValue} is {@code null} or less than or equal to the value,
     * and its {@code maxValue} is {@code null} or greater than the value.
     *
     * @param type        the weather condition rule type
     * @param vehicleType the vehicle type to look up
     * @param value       the numeric value (e.g. temperature or wind speed) to match against
     * @return an {@link Optional} containing the matching fee rule, or empty if none exists
     */
    @Query("""
            select w from WeatherConditionFee w
            where w.type = ?1 and w.vehicleType = ?2 and
                        (w.minValue is null or w.minValue <= ?3) and
                        (w.maxValue is null or w.maxValue > ?3)""")
    Optional<WeatherConditionFee> findByTypeAndVehicleTypeAndNumericValue(WeatherConditionRuleType type, VehicleType vehicleType, Double value);

    /**
     * Finds the weather condition fee rule matching the given type, vehicle type and string value.
     * The rule's {@code stringValue} is matched case-insensitively as a substring of the given value.
     *
     * @param type        the weather condition rule type
     * @param vehicleType the vehicle type to look up
     * @param stringValue the string value (e.g. weather phenomenon) to match against
     * @return an {@link Optional} containing the matching fee rule, or empty if none exists
     */
    @Query("""
            select w from WeatherConditionFee w
            where w.type = ?1 and w.vehicleType = ?2 and upper(?3) like upper(concat('%', w.stringValue, '%'))""")
    Optional<WeatherConditionFee> findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(WeatherConditionRuleType type, VehicleType vehicleType, String stringValue);

}
