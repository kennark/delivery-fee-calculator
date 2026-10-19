package com.example.deliverydemoapi.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 *
 * <p>The handler is exercised in isolation, verifying that each exception is
 * mapped to the expected response body. The HTTP status codes are declared via
 * {@code @ResponseStatus} and are covered by the web layer tests.</p>
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("VehicleForbiddenException handling")
    class VehicleForbiddenExceptionHandling {

        @Test
        @DisplayName("returns the exception message")
        void returnsExceptionMessage() {
            VehicleForbiddenException exception = new VehicleForbiddenException();

            String result = handler.handleVehicleForbiddenException(exception);

            assertThat(result).isEqualTo("Usage of selected vehicle type is forbidden");
        }
    }

    @Nested
    @DisplayName("NoSuchElementException handling")
    class NoSuchElementExceptionHandling {

        @Test
        @DisplayName("returns the exception message")
        void returnsExceptionMessage() {
            String message = "Weather data does not exist for time 2024-01-15T14:30";
            NoSuchElementException exception = new NoSuchElementException(message);

            String result = handler.handleNoSuchElementException(exception);

            assertThat(result).isEqualTo(message);
        }

        @Test
        @DisplayName("returns null when the exception has no message")
        void returnsNullWhenMessageIsAbsent() {
            NoSuchElementException exception = new NoSuchElementException();

            String result = handler.handleNoSuchElementException(exception);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException handling")
    class IllegalArgumentExceptionHandling {

        @Test
        @DisplayName("returns the exception message")
        void returnsExceptionMessage() {
            String message = "Unknown city: HELSINKI";
            IllegalArgumentException exception = new IllegalArgumentException(message);

            String result = handler.handleIllegalArgumentException(exception);

            assertThat(result).isEqualTo(message);
        }

        @Test
        @DisplayName("returns null when the exception has no message")
        void returnsNullWhenMessageIsAbsent() {
            IllegalArgumentException exception = new IllegalArgumentException();

            String result = handler.handleIllegalArgumentException(exception);

            assertThat(result).isNull();
        }
    }
}

