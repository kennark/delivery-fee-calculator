package com.example.deliverydemoapi.data.converters;

import com.example.deliverydemoapi.data.enums.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link StringToVehicleTypeConverter}.
 *
 * <p>The converter is exercised in isolation, verifying that any casing and
 * surrounding whitespace is normalised before resolving the {@link VehicleType} enum.</p>
 */
class StringToVehicleTypeConverterTest {

    private StringToVehicleTypeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToVehicleTypeConverter();
    }

    @Nested
    @DisplayName("Successful conversion")
    class SuccessfulConversion {

        @ParameterizedTest
        @EnumSource(VehicleType.class)
        @DisplayName("converts the exact enum name to the matching VehicleType")
        void convertsExactName(VehicleType vehicleType) {
            assertThat(converter.convert(vehicleType.name())).isEqualTo(vehicleType);
        }

        @ParameterizedTest
        @EnumSource(VehicleType.class)
        @DisplayName("converts a lowercase enum name to the matching VehicleType")
        void convertsLowerCaseName(VehicleType vehicleType) {
            assertThat(converter.convert(vehicleType.name().toLowerCase())).isEqualTo(vehicleType);
        }

        @ParameterizedTest
        @EnumSource(VehicleType.class)
        @DisplayName("converts a mixed-case enum name to the matching VehicleType")
        void convertsMixedCaseName(VehicleType vehicleType) {
            String mixedCase = vehicleType.name().charAt(0)
                    + vehicleType.name().substring(1).toLowerCase();

            assertThat(converter.convert(mixedCase)).isEqualTo(vehicleType);
        }

        @ParameterizedTest
        @EnumSource(VehicleType.class)
        @DisplayName("trims surrounding whitespace before converting")
        void trimsWhitespace(VehicleType vehicleType) {
            assertThat(converter.convert("  " + vehicleType.name().toLowerCase() + "  "))
                    .isEqualTo(vehicleType);
        }
    }

    @Nested
    @DisplayName("Invalid input")
    class InvalidInput {

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "PLANE", "BICYCLE", "123"})
        @DisplayName("throws IllegalArgumentException for unknown vehicle types")
        void throwsForUnknownNames(String source) {
            assertThatThrownBy(() -> converter.convert(source))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws NullPointerException for a null source")
        void throwsForNullSource() {
            String nullSource = null;

            assertThatThrownBy(() -> converter.convert(nullSource))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
