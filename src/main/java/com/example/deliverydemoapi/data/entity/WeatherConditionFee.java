package com.example.deliverydemoapi.data.entity;

import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.data.enums.WeatherConditionRuleType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "weather_condition_fee_rules")
public class WeatherConditionFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private WeatherConditionRuleType type;
    // For use with numeric values
    private Double minValue;
    private Double maxValue;
    // For use with string values
    private String stringValue;
    private Double fee;
    private boolean isAllowed;
    private VehicleType vehicleType;

}
