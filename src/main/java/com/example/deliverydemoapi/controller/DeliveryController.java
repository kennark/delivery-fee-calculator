package com.example.deliverydemoapi.controller;

import com.example.deliverydemoapi.data.dto.DeliveryFeeResponseDto;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.services.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryFeeService deliveryFeeService;

    @GetMapping(path = "/fee")
    public DeliveryFeeResponseDto getDeliveryFee(
            @RequestParam City city,
            @RequestParam VehicleType vehicleType
    ) {
        double fee = deliveryFeeService.calculateFee(city, vehicleType, LocalDateTime.now());

        return new DeliveryFeeResponseDto(fee);
    }

}
