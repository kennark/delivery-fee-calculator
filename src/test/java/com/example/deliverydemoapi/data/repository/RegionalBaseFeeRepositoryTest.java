package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.RegionalBaseFee;
import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
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
 * Repository tests for {@link RegionalBaseFeeRepository}.
 *
 * <p>The test slice runs against an embedded H2 database with the schema generated
 * from the entity mappings and Flyway disabled, so every test works with its own
 * fixtures instead of the production seed data.</p>
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RegionalBaseFeeRepositoryTest {

    @Autowired
    private RegionalBaseFeeRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                new RegionalBaseFee(null, City.TALLINN, VehicleType.CAR, 4.0),
                new RegionalBaseFee(null, City.TALLINN, VehicleType.SCOOTER, 3.5),
                new RegionalBaseFee(null, City.TALLINN, VehicleType.BIKE, 3.0),
                new RegionalBaseFee(null, City.TARTU, VehicleType.CAR, 3.5),
                new RegionalBaseFee(null, City.PÄRNU, VehicleType.BIKE, 2.0)
        ));
    }

    @Nested
    @DisplayName("findByCityAndVehicleType")
    class FindByCityAndVehicleType {

        @Test
        @DisplayName("returns the rule matching the exact city and vehicle type")
        void returnsMatchingRule() {
            Optional<RegionalBaseFee> result =
                    repository.findByCityAndVehicleType(City.TALLINN, VehicleType.CAR);

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("distinguishes rules by vehicle type within the same city")
        void distinguishesByVehicleType() {
            Optional<RegionalBaseFee> result =
                    repository.findByCityAndVehicleType(City.TALLINN, VehicleType.BIKE);

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("distinguishes rules by city for the same vehicle type")
        void distinguishesByCity() {
            Optional<RegionalBaseFee> result =
                    repository.findByCityAndVehicleType(City.PÄRNU, VehicleType.BIKE);

            assertThat(result).isPresent();
            assertThat(result.get().getFee()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("returns empty when the city has no rule for the vehicle type")
        void returnsEmptyWhenVehicleTypeMissing() {
            Optional<RegionalBaseFee> result =
                    repository.findByCityAndVehicleType(City.PÄRNU, VehicleType.SCOOTER);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when the city has no rules at all")
        void returnsEmptyWhenCityMissing() {
            Optional<RegionalBaseFee> result =
                    repository.findByCityAndVehicleType(City.TARTU, VehicleType.SCOOTER);

            assertThat(result).isEmpty();
        }
    }
}
