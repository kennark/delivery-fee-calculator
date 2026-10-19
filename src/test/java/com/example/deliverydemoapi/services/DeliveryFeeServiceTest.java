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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeliveryFeeService}.
 *
 * <p>All repositories are mocked so that the fee calculation logic is exercised
 * in isolation from the persistence layer.</p>
 */
@ExtendWith(MockitoExtension.class)
class DeliveryFeeServiceTest {

    private static final City CITY = City.TALLINN;
    private static final VehicleType VEHICLE = VehicleType.BIKE;
    private static final LocalDateTime TIME = LocalDateTime.of(2023, 11, 15, 12, 0);

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private RegionalBaseFeeRepository regionalBaseFeeRepository;

    @Mock
    private WeatherConditionFeeRepository weatherConditionFeeRepository;

    private DeliveryFeeService service;

    @BeforeEach
    void setUp() {
        service = new DeliveryFeeService(
                weatherDataRepository, regionalBaseFeeRepository, weatherConditionFeeRepository);
    }

    private WeatherData weatherData(double temperature, double windSpeed, String phenomenon) {
        WeatherData data = new WeatherData();
        data.setName(CITY.getStationName());
        data.setTemperature(temperature);
        data.setWindSpeed(windSpeed);
        data.setPhenomenon(phenomenon);
        data.setTimestamp(TIME);
        return data;
    }

    private void stubWeather(WeatherData data) {
        when(weatherDataRepository
                .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                        CITY.getStationName(), TIME))
                .thenReturn(Optional.of(data));
    }

    private void stubRegionalBaseFee(double fee) {
        when(regionalBaseFeeRepository.findByCityAndVehicleType(CITY, VEHICLE))
                .thenReturn(Optional.of(new RegionalBaseFee(null, CITY, VEHICLE, fee)));
    }

    private void stubNumericFee(WeatherConditionRuleType type, double value, double fee, boolean allowed) {
        when(weatherConditionFeeRepository
                .findByTypeAndVehicleTypeAndNumericValue(type, VEHICLE, value))
                .thenReturn(Optional.of(new WeatherConditionFee(
                        null, type, null, null, null, fee, allowed, VEHICLE)));
    }

    private void stubNoNumericFee(WeatherConditionRuleType type, double value) {
        when(weatherConditionFeeRepository
                .findByTypeAndVehicleTypeAndNumericValue(type, VEHICLE, value))
                .thenReturn(Optional.empty());
    }

    private void stubPhenomenonFee(String phenomenon, double fee, boolean allowed) {
        when(weatherConditionFeeRepository
                .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                        WeatherConditionRuleType.WEATHER_PHENOMENON, VEHICLE, phenomenon))
                .thenReturn(Optional.of(new WeatherConditionFee(
                        null, WeatherConditionRuleType.WEATHER_PHENOMENON,
                        null, null, phenomenon, fee, allowed, VEHICLE)));
    }

    @Nested
    @DisplayName("Weather data lookup")
    class WeatherDataLookup {

        @Test
        @DisplayName("looks up the latest weather data for the city's station at the given time")
        void looksUpWeatherDataForStationAndTime() {
            stubWeather(weatherData(10.0, 1.0, "Clear"));
            stubRegionalBaseFee(3.0);

            service.calculateFee(CITY, VEHICLE, TIME);

            verify(weatherDataRepository)
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            CITY.getStationName(), TIME);
        }

        @Test
        @DisplayName("throws NoSuchElementException when no weather data exists for the time")
        void throwsWhenNoWeatherData() {
            when(weatherDataRepository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            CITY.getStationName(), TIME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.calculateFee(CITY, VEHICLE, TIME))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(TIME.toString());

            verifyNoInteractions(regionalBaseFeeRepository, weatherConditionFeeRepository);
        }
    }

    @Nested
    @DisplayName("Regional base fee")
    class RegionalBaseFeeCalculation {

        @Test
        @DisplayName("returns the regional base fee when no extra fees apply")
        void returnsRegionalBaseFeeOnly() {
            stubWeather(weatherData(10.0, 1.0, "Clear"));
            stubRegionalBaseFee(3.5);

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(3.5);
        }

        @Test
        @DisplayName("treats a missing regional base fee rule as zero")
        void missingRegionalBaseFeeIsZero() {
            stubWeather(weatherData(10.0, 1.0, "Clear"));
            when(regionalBaseFeeRepository.findByCityAndVehicleType(CITY, VEHICLE))
                    .thenReturn(Optional.empty());

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isZero();
        }
    }

    @Nested
    @DisplayName("Air temperature extra fee")
    class AirTemperatureExtraFee {

        @Test
        @DisplayName("adds the air temperature extra fee to the total")
        void addsAirTemperatureFee() {
            stubWeather(weatherData(-5.0, 1.0, "Clear"));
            stubRegionalBaseFee(3.0);
            stubNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, -5.0, 1.0, true);

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(4.0);
        }

        @Test
        @DisplayName("throws VehicleForbiddenException when the temperature rule forbids the vehicle")
        void throwsWhenTemperatureForbidsVehicle() {
            stubWeather(weatherData(-20.0, 1.0, "Clear"));
            stubRegionalBaseFee(3.0);
            stubNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, -20.0, 0.0, false);

            assertThatThrownBy(() -> service.calculateFee(CITY, VEHICLE, TIME))
                    .isInstanceOf(VehicleForbiddenException.class)
                    .hasMessage("Usage of selected vehicle type is forbidden");
        }

        @Test
        @DisplayName("adds nothing when no temperature rule matches")
        void noMatchingTemperatureRuleAddsNothing() {
            stubWeather(weatherData(10.0, 1.0, "Clear"));
            stubRegionalBaseFee(3.0);
            when(weatherConditionFeeRepository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VEHICLE, 10.0))
                    .thenReturn(Optional.empty());

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("Wind speed extra fee")
    class WindSpeedExtraFee {

        @Test
        @DisplayName("adds the wind speed extra fee to the total")
        void addsWindSpeedFee() {
            stubWeather(weatherData(10.0, 15.0, "Clear"));
            stubRegionalBaseFee(3.0);
            stubNoNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, 10.0);
            stubNumericFee(WeatherConditionRuleType.WIND_SPEED, 15.0, 0.5, true);

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(3.5);
        }

        @Test
        @DisplayName("throws VehicleForbiddenException when the wind speed rule forbids the vehicle")
        void throwsWhenWindSpeedForbidsVehicle() {
            stubWeather(weatherData(10.0, 25.0, "Clear"));
            stubRegionalBaseFee(3.0);
            stubNoNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, 10.0);
            stubNumericFee(WeatherConditionRuleType.WIND_SPEED, 25.0, 0.0, false);

            assertThatThrownBy(() -> service.calculateFee(CITY, VEHICLE, TIME))
                    .isInstanceOf(VehicleForbiddenException.class);
        }

        @Test
        @DisplayName("adds nothing when no wind speed rule matches")
        void noMatchingWindSpeedRuleAddsNothing() {
            stubWeather(weatherData(10.0, 2.0, "Clear"));
            stubRegionalBaseFee(3.0);
            stubNoNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, 10.0);
            stubNoNumericFee(WeatherConditionRuleType.WIND_SPEED, 2.0);

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("Weather phenomenon extra fee")
    class WeatherPhenomenonExtraFee {

        @Test
        @DisplayName("adds the weather phenomenon extra fee to the total")
        void addsPhenomenonFee() {
            stubWeather(weatherData(10.0, 1.0, "Rain"));
            stubRegionalBaseFee(3.0);
            stubPhenomenonFee("Rain", 0.5, true);

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(3.5);
        }

        @Test
        @DisplayName("throws VehicleForbiddenException when the phenomenon rule forbids the vehicle")
        void throwsWhenPhenomenonForbidsVehicle() {
            stubWeather(weatherData(10.0, 1.0, "Glaze"));
            stubRegionalBaseFee(3.0);
            stubPhenomenonFee("Glaze", 0.0, false);

            assertThatThrownBy(() -> service.calculateFee(CITY, VEHICLE, TIME))
                    .isInstanceOf(VehicleForbiddenException.class);
        }

        @Test
        @DisplayName("adds nothing when no phenomenon rule matches")
        void noMatchingPhenomenonRuleAddsNothing() {
            stubWeather(weatherData(10.0, 1.0, "Clear"));
            stubRegionalBaseFee(3.0);
            when(weatherConditionFeeRepository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VEHICLE, "Clear"))
                    .thenReturn(Optional.empty());

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("Combined fee calculation")
    class CombinedFeeCalculation {

        @Test
        @DisplayName("sums the regional base fee and all applicable extra fees")
        void sumsAllFees() {
            stubWeather(weatherData(-5.0, 15.0, "Rain"));
            stubRegionalBaseFee(3.0);
            stubNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, -5.0, 1.0, true);
            stubNumericFee(WeatherConditionRuleType.WIND_SPEED, 15.0, 0.5, true);
            stubPhenomenonFee("Rain", 0.5, true);

            double fee = service.calculateFee(CITY, VEHICLE, TIME);

            assertThat(fee).isEqualTo(5.0);
        }

        @Test
        @DisplayName("queries each rule type with the vehicle type and weather values")
        void queriesEachRuleTypeWithWeatherValues() {
            stubWeather(weatherData(-5.0, 15.0, "Rain"));
            stubRegionalBaseFee(3.0);
            stubNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, -5.0, 1.0, true);
            stubNumericFee(WeatherConditionRuleType.WIND_SPEED, 15.0, 0.5, true);
            stubPhenomenonFee("Rain", 0.5, true);

            service.calculateFee(CITY, VEHICLE, TIME);

            verify(regionalBaseFeeRepository).findByCityAndVehicleType(CITY, VEHICLE);
            verify(weatherConditionFeeRepository)
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VEHICLE, -5.0);
            verify(weatherConditionFeeRepository)
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.WIND_SPEED, VEHICLE, 15.0);
            verify(weatherConditionFeeRepository)
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VEHICLE, "Rain");
        }

        @Test
        @DisplayName("stops at the first forbidding rule and does not evaluate later rules")
        void stopsAtFirstForbiddingRule() {
            stubWeather(weatherData(-20.0, 15.0, "Rain"));
            stubRegionalBaseFee(3.0);
            stubNumericFee(WeatherConditionRuleType.AIR_TEMPERATURE, -20.0, 0.0, false);

            assertThatThrownBy(() -> service.calculateFee(CITY, VEHICLE, TIME))
                    .isInstanceOf(VehicleForbiddenException.class);

            verify(weatherConditionFeeRepository, never())
                    .findByTypeAndVehicleTypeAndNumericValue(
                            eq(WeatherConditionRuleType.WIND_SPEED), eq(VEHICLE), anyDouble());
            verify(weatherConditionFeeRepository, never())
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            eq(WeatherConditionRuleType.WEATHER_PHENOMENON), eq(VEHICLE), anyString());
        }
    }
}

