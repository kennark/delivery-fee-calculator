package com.example.deliverydemoapi.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "weather_data")
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String wmoCode;
    private double temperature;
    private double windSpeed;
    private String phenomenon;
    private LocalDateTime timestamp;

}
