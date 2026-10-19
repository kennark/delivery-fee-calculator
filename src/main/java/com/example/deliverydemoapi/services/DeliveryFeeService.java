package com.example.deliverydemoapi.services;

import com.example.deliverydemoapi.data.entity.WeatherData;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.data.repository.WeatherDataRepository;
import com.example.deliverydemoapi.exceptions.VehicleForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryFeeService {

    private final WeatherDataRepository weatherDataRepository;


    public double calculateFee(City city, VehicleType vehicleType, LocalDateTime time) {
        WeatherData weatherData = weatherDataRepository
                .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(city.getStationName(), time)
                .orElseThrow();

        double totalExtraFee = 0;
        totalExtraFee += calculateRegionalBaseFee(city, vehicleType);

        totalExtraFee += calculateATEF(vehicleType, weatherData.getTemperature());
        totalExtraFee += calculateWSEF(vehicleType, weatherData.getWindSpeed());
        totalExtraFee += calculateWPEF(vehicleType, weatherData.getPhenomenon());

        return totalExtraFee;
    }


    private double calculateRegionalBaseFee(City city, VehicleType vehicleType) {
        if (city == City.TALLINN) {
            return switch (vehicleType) {
                case CAR -> 4;
                case SCOOTER -> 3.5;
                case BIKE -> 3;
            };
        } else if (city == City.TARTU) {
            return switch (vehicleType) {
                case CAR -> 3.5;
                case SCOOTER -> 3;
                case BIKE -> 2.5;
            };
        } else if (city == City.PÄRNU) {
            return switch (vehicleType) {
                case CAR -> 3;
                case SCOOTER -> 2.5;
                case BIKE -> 2;
            };
        }

        // This should never reach ideally
        throw new IllegalArgumentException();
    }

    private double calculateATEF(VehicleType vehicleType, double temperature) {
        if (vehicleType == VehicleType.CAR) return 0;

        if (temperature < -10.0) return 1;
        if (temperature <= 0) return 0.5;
        return 0;
    }

    private double calculateWSEF(VehicleType vehicleType, double windSpeed) {
        if (vehicleType == VehicleType.BIKE) {
            if (windSpeed >= 10 && windSpeed <= 20) return 0.5;
            if (windSpeed > 20) throw new VehicleForbiddenException();
        }
        return 0;
    }

    private double calculateWPEF(VehicleType vehicleType, String phenomenon) {
        if (vehicleType == VehicleType.CAR) return 0;
        if (phenomenon == null || phenomenon.isEmpty()) return 0;

        phenomenon = phenomenon.toLowerCase();

        if (phenomenon.contains("snow") || phenomenon.contains("sleet")) return 1;
        if (phenomenon.contains("rain")) return 0.5;

        if (phenomenon.contains("glaze") || phenomenon.contains("hail") || phenomenon.contains("thunder"))
            throw new VehicleForbiddenException();

        return 0;
    }


}
