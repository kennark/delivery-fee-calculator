package com.example.deliverydemoapi.controller;

import com.example.deliverydemoapi.data.enums.City;
import com.example.deliverydemoapi.data.enums.VehicleType;
import com.example.deliverydemoapi.exceptions.VehicleForbiddenException;
import com.example.deliverydemoapi.services.DeliveryFeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link DeliveryController}.
 *
 * <p>The controller is exercised through {@link MockMvc} with the
 * {@link DeliveryFeeService} replaced by a mock, so that only the request
 * binding, the response serialization and the exception mapping are verified.
 * The {@code StringToCityConverter} and {@code StringToVehicleTypeConverter}
 * beans and the {@code GlobalExceptionHandler} are picked up by the
 * {@link WebMvcTest} slice.</p>
 */
@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

    private static final String FEE_ENDPOINT = "/delivery/fee";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryFeeService deliveryFeeService;

    @Nested
    @DisplayName("Successful fee calculation")
    class SuccessfulFeeCalculation {

        @Test
        @DisplayName("returns 200 with the calculated fee as JSON")
        void returnsCalculatedFee() throws Exception {
            when(deliveryFeeService.calculateFee(eq(City.TALLINN), eq(VehicleType.BIKE), any()))
                    .thenReturn(3.5);

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN")
                            .param("vehicleType", "BIKE"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fee").value(3.5));
        }

        @Test
        @DisplayName("passes the provided time to the service")
        void passesProvidedTimeToService() throws Exception {
            LocalDateTime time = LocalDateTime.of(2024, 1, 15, 14, 30);
            when(deliveryFeeService.calculateFee(any(), any(), any())).thenReturn(1.0);

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN")
                            .param("vehicleType", "BIKE")
                            .param("time", "2024-01-15T14:30:00"))
                    .andExpect(status().isOk());

            verify(deliveryFeeService).calculateFee(City.TALLINN, VehicleType.BIKE, time);
        }

        @Test
        @DisplayName("uses the current time when no time is provided")
        void usesCurrentTimeWhenTimeIsOmitted() throws Exception {
            when(deliveryFeeService.calculateFee(any(), any(), any())).thenReturn(1.0);
            LocalDateTime before = LocalDateTime.now();

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN")
                            .param("vehicleType", "BIKE"))
                    .andExpect(status().isOk());

            LocalDateTime after = LocalDateTime.now();
            ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(deliveryFeeService)
                    .calculateFee(eq(City.TALLINN), eq(VehicleType.BIKE), timeCaptor.capture());

            assertThat(timeCaptor.getValue()).isBetween(before, after);
        }

        @Test
        @DisplayName("accepts lowercase city and vehicle type values")
        void acceptsLowerCaseValues() throws Exception {
            when(deliveryFeeService.calculateFee(any(), any(), any())).thenReturn(1.0);

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "tallinn")
                            .param("vehicleType", "bike"))
                    .andExpect(status().isOk());

            verify(deliveryFeeService).calculateFee(eq(City.TALLINN), eq(VehicleType.BIKE), any());
        }

        @Test
        @DisplayName("accepts a city name with non-ASCII characters")
        void acceptsNonAsciiCityName() throws Exception {
            when(deliveryFeeService.calculateFee(any(), any(), any())).thenReturn(1.0);

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "pärnu")
                            .param("vehicleType", "car"))
                    .andExpect(status().isOk());

            verify(deliveryFeeService).calculateFee(eq(City.PÄRNU), eq(VehicleType.CAR), any());
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("returns 403 with the exception message when the vehicle is forbidden")
        void returnsForbiddenWhenVehicleIsForbidden() throws Exception {
            when(deliveryFeeService.calculateFee(any(), any(), any()))
                    .thenThrow(new VehicleForbiddenException());

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN")
                            .param("vehicleType", "BIKE"))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Usage of selected vehicle type is forbidden"));
        }

        @Test
        @DisplayName("returns 404 with the exception message when no weather data exists")
        void returnsNotFoundWhenNoWeatherData() throws Exception {
            String message = "Weather data does not exist for time 2024-01-15T14:30";
            when(deliveryFeeService.calculateFee(any(), any(), any()))
                    .thenThrow(new NoSuchElementException(message));

            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN")
                            .param("vehicleType", "BIKE"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(message));
        }

        @Test
        @DisplayName("returns 400 for an unknown city")
        void returnsBadRequestForUnknownCity() throws Exception {
            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "HELSINKI")
                            .param("vehicleType", "BIKE"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(deliveryFeeService);
        }

        @Test
        @DisplayName("returns 400 for an unknown vehicle type")
        void returnsBadRequestForUnknownVehicleType() throws Exception {
            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN")
                            .param("vehicleType", "PLANE"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(deliveryFeeService);
        }

        @Test
        @DisplayName("returns 400 when the city parameter is missing")
        void returnsBadRequestWhenCityIsMissing() throws Exception {
            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("vehicleType", "BIKE"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(deliveryFeeService);
        }

        @Test
        @DisplayName("returns 400 when the vehicle type parameter is missing")
        void returnsBadRequestWhenVehicleTypeIsMissing() throws Exception {
            mockMvc.perform(get(FEE_ENDPOINT)
                            .param("city", "TALLINN"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(deliveryFeeService);
        }
    }
}

