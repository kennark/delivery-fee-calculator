package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.WeatherConditionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherConditionFeeRepository extends JpaRepository<WeatherConditionFee, Long> {
}
