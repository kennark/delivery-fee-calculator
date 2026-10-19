package com.example.deliverydemoapi.data.entity;

import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "regional_base_fee_rules")
public class RegionalBaseFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private City city;
    private VehicleType vehicleType;
    private double fee;

}
