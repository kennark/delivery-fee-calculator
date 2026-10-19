package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.WeatherConditionFee;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.data.enums.WeatherConditionRuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link WeatherConditionFeeRepository}.
 *
 * <p>The test slice runs against an embedded H2 database with the schema generated
 * from the entity mappings and Flyway disabled, so every test works with its own
 * fixtures instead of the production seed data.</p>
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WeatherConditionFeeRepositoryTest {

    @Autowired
    private WeatherConditionFeeRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                // AIR_TEMPERATURE, BIKE: open lower bound, exclusive upper bound
                new WeatherConditionFee(null, WeatherConditionRuleType.AIR_TEMPERATURE,
                        null, -10.0, null, 1.0, true, VehicleType.BIKE),
                new WeatherConditionFee(null, WeatherConditionRuleType.AIR_TEMPERATURE,
                        -10.0, 0.0, null, 0.5, true, VehicleType.BIKE),
                // AIR_TEMPERATURE, SCOOTER: same range as BIKE to prove vehicle filtering
                new WeatherConditionFee(null, WeatherConditionRuleType.AIR_TEMPERATURE,
                        -10.0, 0.0, null, 0.5, true, VehicleType.SCOOTER),
                // WIND_SPEED, BIKE: open upper bound, forbidden
                new WeatherConditionFee(null, WeatherConditionRuleType.WIND_SPEED,
                        20.0, null, null, 0.0, false, VehicleType.BIKE),
                // WEATHER_PHENOMENON, BIKE
                new WeatherConditionFee(null, WeatherConditionRuleType.WEATHER_PHENOMENON,
                        null, null, "snow", 1.0, true, VehicleType.BIKE),
                new WeatherConditionFee(null, WeatherConditionRuleType.WEATHER_PHENOMENON,
                        null, null, "glaze", 0.5, false, VehicleType.BIKE),
                // WEATHER_PHENOMENON, SCOOTER: same phenomenon to prove vehicle filtering
                new WeatherConditionFee(null, WeatherConditionRuleType.WEATHER_PHENOMENON,
                        null, null, "snow", 1.0, true, VehicleType.SCOOTER)
        ));
    }

    @Nested
    @DisplayName("findByTypeAndVehicleTypeAndNumericValue")
    class FindByNumericValue {

        @Test
        @DisplayName("matches a value inside a bounded range")
        void matchesValueInsideBoundedRange() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VehicleType.BIKE, -5.0);

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("treats minValue as inclusive")
        void treatsMinValueAsInclusive() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VehicleType.BIKE, -10.0);

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("treats maxValue as exclusive")
        void treatsMaxValueAsExclusive() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VehicleType.BIKE, 0.0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("matches a value below a null minValue")
        void matchesValueBelowNullMinValue() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VehicleType.BIKE, -20.0);

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("matches a value above a null maxValue")
        void matchesValueAboveNullMaxValue() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.WIND_SPEED, VehicleType.BIKE, 25.0);

            assertThat(result).isPresent();
            assertThat(result.get().isAllowed()).isFalse();
        }

        @Test
        @DisplayName("filters by vehicle type")
        void filtersByVehicleType() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VehicleType.SCOOTER, -5.0);

            assertThat(result).isPresent();
            assertThat(result.get().getVehicleType()).isEqualTo(VehicleType.SCOOTER);
        }

        @Test
        @DisplayName("filters by rule type")
        void filtersByRuleType() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.WIND_SPEED, VehicleType.BIKE, -5.0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when no range matches the value")
        void returnsEmptyWhenNoRangeMatches() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndNumericValue(
                            WeatherConditionRuleType.WIND_SPEED, VehicleType.BIKE, 5.0);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase")
    class FindByStringValue {

        @Test
        @DisplayName("matches the stored string value exactly")
        void matchesExactStringValue() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VehicleType.BIKE, "snow");

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("matches the stored string value case-insensitively")
        void matchesStringValueIgnoringCase() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VehicleType.BIKE, "SNOW");

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("matches when the stored value is a substring of the given value")
        void matchesWhenStoredValueIsSubstring() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VehicleType.BIKE,
                            "Light snow");

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("filters by vehicle type")
        void filtersByVehicleType() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VehicleType.SCOOTER, "snow");

            assertThat(result).isPresent();
            assertThat(result.get().getVehicleType()).isEqualTo(VehicleType.SCOOTER);
        }

        @Test
        @DisplayName("filters by rule type")
        void filtersByRuleType() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.AIR_TEMPERATURE, VehicleType.BIKE, "snow");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when no string value matches")
        void returnsEmptyWhenNoStringValueMatches() {
            Optional<WeatherConditionFee> result = repository
                    .findByTypeAndVehicleTypeAndStringValueLikeIgnoreCase(
                            WeatherConditionRuleType.WEATHER_PHENOMENON, VehicleType.BIKE, "clear");

            assertThat(result).isEmpty();
        }
    }
}
