package com.example.deliverydemoapi.services;

import com.example.deliverydemoapi.data.dto.ObservationResponseDto;
import com.example.deliverydemoapi.data.dto.ObservationResponseItem;
import com.example.deliverydemoapi.data.entity.WeatherData;
import com.example.deliverydemoapi.data.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherFetchService {

    private final WeatherDataRepository weatherDataRepository;
    private final RestClient client;

    @Value("#{'${weather.stations}'.split(',')}")
    private List<String> targetStations;

    @Value("${weather.api.endpoint}")
    private String endpoint;

    /**
     * Fetches the latest weather observations from the configured weather API and
     * persists the data for the configured target stations. This method is invoked
     * on the schedule defined by the {@code weather.schedule} property.
     */
    @Scheduled(cron = "${weather.schedule:0 15 * * * ?}")
    public void fetchData() {

        ObservationResponseDto observations = client.get()
                .uri(endpoint)
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .body(ObservationResponseDto.class);

        if (observations != null && !observations.stations().isEmpty()) {

            LocalDateTime timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(observations.timestamp()),
                    ZoneId.of("Europe/Tallinn"));

            observations.stations()
                    .parallelStream()
                    .filter(station ->
                            targetStations.contains(station.name()))
                    .forEach(station ->
                            saveWeatherEntity(createWeatherDataEntity(station, timestamp)));
        }
    }

    private WeatherData createWeatherDataEntity(ObservationResponseItem station, LocalDateTime timestamp) {
        WeatherData entity = new WeatherData();
        entity.setName(station.name());
        entity.setPhenomenon(station.phenomenon());
        entity.setTemperature(station.temperature());
        entity.setWindSpeed(station.windSpeed());
        entity.setWmoCode(station.wmoCode());
        entity.setTimestamp(timestamp);

        return entity; // ID is generated on save
    }


    private void saveWeatherEntity(WeatherData entity) {
        weatherDataRepository.save(entity);
    }
}
