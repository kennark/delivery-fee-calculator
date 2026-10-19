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

    /**
     * Calculates the total delivery fee for a given city, vehicle type and time.
     * If no time is provided, the current time is used.
     *
     * @param city        the city the delivery is made in
     * @param vehicleType the type of vehicle used for the delivery
     * @param time        the time at which the delivery fee should be evaluated; defaults to now if {@code null}
     * @return a {@link DeliveryFeeResponseDto} containing the calculated delivery fee
     */
    @GetMapping(path = "/fee")
    public DeliveryFeeResponseDto getDeliveryFee(
            @RequestParam City city,
            @RequestParam VehicleType vehicleType,
            @RequestParam(required = false) LocalDateTime time
    ) {
        if (time == null)
            time = LocalDateTime.now();

        double fee = deliveryFeeService.calculateFee(city, vehicleType, time);

        return new DeliveryFeeResponseDto(fee);
    }

}
