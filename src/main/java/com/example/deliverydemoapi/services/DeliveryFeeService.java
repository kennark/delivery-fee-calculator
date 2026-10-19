package com.example.deliverydemoapi.services;

import com.example.deliverydemoapi.data.entity.RegionalBaseFee;
import com.example.deliverydemoapi.data.entity.WeatherConditionFee;
import com.example.deliverydemoapi.data.entity.WeatherData;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.data.enums.WeatherConditionRuleType;
import com.example.deliverydemoapi.data.repository.RegionalBaseFeeRepository;
import com.example.deliverydemoapi.data.repository.WeatherConditionFeeRepository;
import com.example.deliverydemoapi.data.repository.WeatherDataRepository;
import com.example.deliverydemoapi.exceptions.VehicleForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryFeeService {

    private final WeatherDataRepository weatherDataRepository;
    private final RegionalBaseFeeRepository regionalBaseFeeRepository;
    private final WeatherConditionFeeRepository weatherConditionFeeRepository;


    /**
     * Calculates the total delivery fee for the given city, vehicle type and time.
     * The fee is composed of the regional base fee plus any applicable extra fees
     * based on air temperature, wind speed and weather phenomenon.
     *
     * @param city        the city the delivery is made in
     * @param vehicleType the type of vehicle used for the delivery
     * @param time        the time at which the fee should be evaluated
     * @return the total delivery fee
     * @throws java.util.NoSuchElementException if no weather data exists for the given time
     * @throws VehicleForbiddenException        if the selected vehicle type is forbidden under the current weather conditions
     */
    public double calculateFee(City city, VehicleType vehicleType, LocalDateTime time) {
        WeatherData weatherData = weatherDataRepository
                .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(city.getStationName(), time)
                .orElseThrow(() -> new NoSuchElementException("Weather data does not exist for time %s".formatted(time)));

        double totalExtraFee = 0;
        totalExtraFee += calculateRegionalBaseFee(city, vehicleType);

        totalExtraFee += calculateATEF(vehicleType, weatherData.getTemperature());
        totalExtraFee += calculateWSEF(vehicleType, weatherData.getWindSpeed());
        totalExtraFee += calculateWPEF(vehicleType, weatherData.getPhenomenon());

        return totalExtraFee;
    }

    private double calculateRegionalBaseFee(City city, VehicleType vehicleType) {

        Optional<RegionalBaseFee> fee = regionalBaseFeeRepository
                .findByCityAndVehicleType(city, vehicleType);

        if (fee.isEmpty()) return 0.0;

        return fee.get().getFee();
    }

    private double calculateATEF(VehicleType vehicleType, double temperature) {

        Optional<WeatherConditionFee> feeOptional = weatherConditionFeeRepository.findByTypeAndVehicleTypeAndNumericValue(WeatherConditionRuleType.AIR_TEMPERATURE, vehicleType, temperature);

        if (feeOptional.isEmpty()) return 0.0;

        WeatherConditionFee fee = feeOptional.get();
        if (!fee.isAllowed())
            throw new VehicleForbiddenException();
        return fee.getFee();
    }

    private double calculateWSEF(VehicleType vehicleType, double windSpeed) {

        Optional<WeatherConditionFee> feeOptional = weatherConditionFeeRepository.findByTypeAndVehicleTypeAndNumericValue(WeatherConditionRuleType.WIND_SPEED, vehicleType, windSpeed);

        if (feeOptional.isEmpty()) return 0.0;

        WeatherConditionFee fee = feeOptional.get();
        if (!fee.isAllowed())
            throw new VehicleForbiddenException();
        return fee.getFee();

    }

    private double calculateWPEF(VehicleType vehicleType, String phenomenon) {

        Optional<WeatherConditionFee> feeOptional = weatherConditionFeeRepository.findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(WeatherConditionRuleType.WEATHER_PHENOMENON, vehicleType, phenomenon);

        if (feeOptional.isEmpty()) return 0.0;

        WeatherConditionFee fee = feeOptional.get();
        if (!fee.isAllowed())
            throw new VehicleForbiddenException();
        return fee.getFee();
    }
}
