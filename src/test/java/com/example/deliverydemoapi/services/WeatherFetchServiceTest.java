package com.example.deliverydemoapi.services;

import com.example.deliverydemoapi.data.dto.ObservationResponseDto;
import com.example.deliverydemoapi.data.dto.ObservationResponseItem;
import com.example.deliverydemoapi.data.entity.WeatherData;
import com.example.deliverydemoapi.data.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WeatherFetchService}.
 *
 * <p>The {@link RestClient} fluent call chain and the {@link WeatherDataRepository}
 * are mocked so that the service is exercised in isolation.</p>
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class WeatherFetchServiceTest {

    private static final String ENDPOINT = "ilma_andmed/xml/observations.php";
    private static final List<String> TARGET_STATIONS =
            List.of("Tallinn-Harku", "Tartu-Tõravere", "Pärnu");

    /**
     * 2023-11-14T22:13:20Z, which is 2023-11-15T00:13:20 in Europe/Tallinn (EET, UTC+2).
     */
    private static final long EPOCH_SECONDS = 1_700_000_000L;
    private static final LocalDateTime EXPECTED_TALLINN_TIMESTAMP =
            LocalDateTime.of(2023, 11, 15, 0, 13, 20);

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private RestClient client;

    @Mock
    private RestClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private WeatherFetchService service;

    private static ObservationResponseItem station(String name, String wmoCode,
                                                   Double temperature, Double windSpeed,
                                                   String phenomenon) {
        return new ObservationResponseItem(name, wmoCode, temperature, windSpeed, phenomenon);
    }

    @BeforeEach
    void setUp() {
        service = new WeatherFetchService(weatherDataRepository, client);
        ReflectionTestUtils.setField(service, "targetStations", TARGET_STATIONS);
        ReflectionTestUtils.setField(service, "endpoint", ENDPOINT);
    }

    private void stubClient(ObservationResponseDto response) {
        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri(ENDPOINT)).thenReturn(requestSpec);
        when(requestSpec.accept(MediaType.APPLICATION_XML)).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ObservationResponseDto.class)).thenReturn(response);
    }

    @Nested
    @DisplayName("HTTP interaction")
    class HttpInteraction {

        @Test
        @DisplayName("requests XML observations from the configured endpoint")
        void requestsXmlFromConfiguredEndpoint() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of()));

            service.fetchData();

            verify(client).get();
            verify(uriSpec).uri(ENDPOINT);
            verify(requestSpec).accept(MediaType.APPLICATION_XML);
            verify(requestSpec).retrieve();
            verify(responseSpec).body(ObservationResponseDto.class);
        }
    }

    @Nested
    @DisplayName("Filtering")
    class Filtering {

        @Test
        @DisplayName("persists only the observations of the configured target stations")
        void savesOnlyTargetStations() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Tallinn-Harku", "26038", 1.5, 3.2, "Clear"),
                    station("Tartu-Tõravere", "26242", -2.0, 1.0, "Cloudy"),
                    station("Narva", "26058", 5.0, 2.0, "Rain")
            )));

            service.fetchData();

            ArgumentCaptor<WeatherData> captor = ArgumentCaptor.forClass(WeatherData.class);
            verify(weatherDataRepository, times(2)).save(captor.capture());

            assertThat(captor.getAllValues())
                    .extracting(WeatherData::getName)
                    .containsExactlyInAnyOrder("Tallinn-Harku", "Tartu-Tõravere");
        }

        @Test
        @DisplayName("persists every observation when all stations are targets")
        void savesAllStationsWhenAllAreTargets() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Tallinn-Harku", "26038", 1.5, 3.2, "Clear"),
                    station("Tartu-Tõravere", "26242", -2.0, 1.0, "Cloudy"),
                    station("Pärnu", "41803", 4.0, 6.0, "Rain")
            )));

            service.fetchData();

            verify(weatherDataRepository, times(3)).save(any(WeatherData.class));
        }

        @Test
        @DisplayName("persists nothing when no station matches the target list")
        void savesNothingWhenNoStationMatches() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Narva", "26058", 5.0, 2.0, "Rain"),
                    station("Kuressaare", "26134", 3.0, 4.0, "Cloudy")
            )));

            service.fetchData();

            verify(weatherDataRepository, never()).save(any(WeatherData.class));
        }

        @Test
        @DisplayName("matches station names case-sensitively")
        void doesNotMatchStationNamesWithDifferentCase() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("tallinn-harku", "26038", 1.5, 3.2, "Clear")
            )));

            service.fetchData();

            verify(weatherDataRepository, never()).save(any(WeatherData.class));
        }
    }

    @Nested
    @DisplayName("Entity mapping")
    class EntityMapping {

        @Test
        @DisplayName("maps every observation field onto the persisted entity")
        void mapsAllFieldsToEntity() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Tallinn-Harku", "26038", 1.5, 3.2, "Clear")
            )));

            service.fetchData();

            ArgumentCaptor<WeatherData> captor = ArgumentCaptor.forClass(WeatherData.class);
            verify(weatherDataRepository).save(captor.capture());

            WeatherData saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("Tallinn-Harku");
            assertThat(saved.getWmoCode()).isEqualTo("26038");
            assertThat(saved.getTemperature()).isEqualTo(1.5);
            assertThat(saved.getWindSpeed()).isEqualTo(3.2);
            assertThat(saved.getPhenomenon()).isEqualTo("Clear");
        }

        @Test
        @DisplayName("converts the epoch timestamp to a Europe/Tallinn LocalDateTime")
        void convertsEpochTimestampToTallinnLocalDateTime() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Tallinn-Harku", "26038", 1.5, 3.2, "Clear")
            )));

            service.fetchData();

            ArgumentCaptor<WeatherData> captor = ArgumentCaptor.forClass(WeatherData.class);
            verify(weatherDataRepository).save(captor.capture());

            assertThat(captor.getValue().getTimestamp()).isEqualTo(EXPECTED_TALLINN_TIMESTAMP);
        }

        @Test
        @DisplayName("applies the same timestamp to every persisted entity")
        void appliesSameTimestampToAllEntities() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Tallinn-Harku", "26038", 1.5, 3.2, "Clear"),
                    station("Pärnu", "41803", 4.0, 6.0, "Rain")
            )));

            service.fetchData();

            ArgumentCaptor<WeatherData> captor = ArgumentCaptor.forClass(WeatherData.class);
            verify(weatherDataRepository, times(2)).save(captor.capture());

            assertThat(captor.getAllValues())
                    .extracting(WeatherData::getTimestamp)
                    .containsOnly(EXPECTED_TALLINN_TIMESTAMP);
        }

        @Test
        @DisplayName("leaves the generated id unset before persisting")
        void leavesIdUnsetBeforePersisting() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of(
                    station("Tallinn-Harku", "26038", 1.5, 3.2, "Clear")
            )));

            service.fetchData();

            ArgumentCaptor<WeatherData> captor = ArgumentCaptor.forClass(WeatherData.class);
            verify(weatherDataRepository).save(captor.capture());

            assertThat(captor.getValue().getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Empty or missing responses")
    class EmptyResponses {

        @Test
        @DisplayName("does nothing when the API returns no body")
        void doesNothingWhenResponseIsNull() {
            stubClient(null);

            service.fetchData();

            verifyNoInteractions(weatherDataRepository);
        }

        @Test
        @DisplayName("does nothing when the response contains no stations")
        void doesNothingWhenStationListIsEmpty() {
            stubClient(new ObservationResponseDto(EPOCH_SECONDS, List.of()));

            service.fetchData();

            verifyNoInteractions(weatherDataRepository);
        }
    }
}
