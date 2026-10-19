package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /**
     * Finds the most recent weather data entry for the given station name at or before the given timestamp.
     *
     * @param name      the station name to look up (case-insensitive)
     * @param timestamp the upper bound timestamp (inclusive)
     * @return an {@link Optional} containing the latest matching weather data, or empty if none exists
     */
    Optional<WeatherData> findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(String name, LocalDateTime timestamp);

}
