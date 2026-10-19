package com.example.deliverydemoapi.controller;

import com.example.deliverydemoapi.data.dto.DeliveryFeeResponseDto;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.services.DeliveryFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "Operations for calculating delivery fees based on city, vehicle type and weather conditions")
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
    @Operation(
            summary = "Calculate the delivery fee",
            description = "Calculates the total delivery fee for the given city and vehicle type, "
                    + "taking into account the regional base fee and any weather-based extra fees "
                    + "(air temperature, wind speed and weather phenomenon). "
                    + "If no time is provided, the current time is used."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery fee calculated successfully",
                    content = @Content(schema = @Schema(implementation = DeliveryFeeResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters (e.g. unknown city or vehicle type)",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The selected vehicle type is forbidden under the current weather conditions",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No weather data exists for the given time",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @GetMapping(path = "/fee")
    public DeliveryFeeResponseDto getDeliveryFee(
            @Parameter(description = "The city the delivery is made in", example = "TALLINN", required = true)
            @RequestParam City city,
            @Parameter(description = "The type of vehicle used for the delivery", example = "BIKE", required = true)
            @RequestParam VehicleType vehicleType,
            @Parameter(description = "The time at which the delivery fee should be evaluated (ISO-8601). Defaults to the current time if omitted.", example = "2024-01-15T14:30:00")
            @RequestParam(required = false) LocalDateTime time
    ) {
        if (time == null)
            time = LocalDateTime.now();

        double fee = deliveryFeeService.calculateFee(city, vehicleType, time);

        return new DeliveryFeeResponseDto(fee);
    }

}
