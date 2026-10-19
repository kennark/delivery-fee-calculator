package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link WeatherDataRepository}.
 *
 * <p>The test slice runs against an embedded H2 database with the schema generated
 * from the entity mappings and Flyway disabled, so every test works with its own
 * fixtures instead of the production seed data.</p>
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WeatherDataRepositoryTest {

    private static final String STATION = "Tallinn-Harku";
    private static final LocalDateTime EARLIEST = LocalDateTime.of(2023, 11, 15, 6, 0);
    private static final LocalDateTime MIDDLE = LocalDateTime.of(2023, 11, 15, 12, 0);
    private static final LocalDateTime LATEST = LocalDateTime.of(2023, 11, 15, 18, 0);

    @Autowired
    private WeatherDataRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                weatherData(STATION, EARLIEST, 1.0),
                weatherData(STATION, MIDDLE, 2.0),
                weatherData(STATION, LATEST, 3.0),
                weatherData("Tartu-Tõravere", MIDDLE, 4.0)
        ));
    }

    private WeatherData weatherData(String name, LocalDateTime timestamp, double temperature) {
        WeatherData data = new WeatherData();
        data.setName(name);
        data.setTimestamp(timestamp);
        data.setTemperature(temperature);
        data.setWindSpeed(1.0);
        data.setPhenomenon("Clear");
        return data;
    }

    @Nested
    @DisplayName("findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc")
    class FindLatestByNameAndTimestamp {

        @Test
        @DisplayName("returns the most recent entry at or before the given timestamp")
        void returnsMostRecentEntry() {
            Optional<WeatherData> result = repository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            STATION, MIDDLE);

            assertThat(result).isPresent();
            assertThat(result.get().getTimestamp()).isEqualTo(MIDDLE);
            assertThat(result.get().getTemperature()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("includes an entry whose timestamp equals the given timestamp")
        void includesEntryAtExactTimestamp() {
            Optional<WeatherData> result = repository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            STATION, LATEST);

            assertThat(result).isPresent();
            assertThat(result.get().getTimestamp()).isEqualTo(LATEST);
        }

        @Test
        @DisplayName("ignores entries newer than the given timestamp")
        void ignoresNewerEntries() {
            Optional<WeatherData> result = repository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            STATION, MIDDLE.plusMinutes(1));

            assertThat(result).isPresent();
            assertThat(result.get().getTimestamp()).isEqualTo(MIDDLE);
        }

        @Test
        @DisplayName("matches the station name case-insensitively")
        void matchesNameIgnoringCase() {
            Optional<WeatherData> result = repository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            "tallinn-harku", LATEST);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(STATION);
        }

        @Test
        @DisplayName("returns empty when the timestamp precedes every entry")
        void returnsEmptyWhenTimestampTooEarly() {
            Optional<WeatherData> result = repository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            STATION, EARLIEST.minusSeconds(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when the station has no entries")
        void returnsEmptyWhenStationUnknown() {
            Optional<WeatherData> result = repository
                    .findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(
                            "Pärnu", LATEST);

            assertThat(result).isEmpty();
        }
    }
}
