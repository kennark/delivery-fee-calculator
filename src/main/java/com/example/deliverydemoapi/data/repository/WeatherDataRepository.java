package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    Optional<WeatherData> findFirstByNameIgnoreCaseAndTimestampLessThanEqualOrderByTimestampDesc(String name, LocalDateTime timestamp);

}
