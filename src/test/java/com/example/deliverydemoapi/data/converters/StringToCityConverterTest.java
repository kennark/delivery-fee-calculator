package com.example.deliverydemoapi.data.converters;

import com.example.deliverydemoapi.data.enums.City;
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
 * Unit tests for {@link StringToCityConverter}.
 *
 * <p>The converter is exercised in isolation, verifying that any casing and
 * surrounding whitespace is normalised before resolving the {@link City} enum.</p>
 */
class StringToCityConverterTest {

    private StringToCityConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToCityConverter();
    }

    @Nested
    @DisplayName("Successful conversion")
    class SuccessfulConversion {

        @ParameterizedTest
        @EnumSource(City.class)
        @DisplayName("converts the exact enum name to the matching City")
        void convertsExactName(City city) {
            assertThat(converter.convert(city.name())).isEqualTo(city);
        }

        @ParameterizedTest
        @EnumSource(City.class)
        @DisplayName("converts a lowercase enum name to the matching City")
        void convertsLowerCaseName(City city) {
            assertThat(converter.convert(city.name().toLowerCase())).isEqualTo(city);
        }

        @ParameterizedTest
        @EnumSource(City.class)
        @DisplayName("converts a mixed-case enum name to the matching City")
        void convertsMixedCaseName(City city) {
            String mixedCase = city.name().charAt(0)
                    + city.name().substring(1).toLowerCase();

            assertThat(converter.convert(mixedCase)).isEqualTo(city);
        }

        @ParameterizedTest
        @EnumSource(City.class)
        @DisplayName("trims surrounding whitespace before converting")
        void trimsWhitespace(City city) {
            assertThat(converter.convert("  " + city.name().toLowerCase() + "  ")).isEqualTo(city);
        }

        @Test
        @DisplayName("converts a value with non-ASCII characters")
        void convertsNonAsciiValue() {
            assertThat(converter.convert("pärnu")).isEqualTo(City.PÄRNU);
        }
    }

    @Nested
    @DisplayName("Invalid input")
    class InvalidInput {

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "Helsinki", "Tallin", "123"})
        @DisplayName("throws IllegalArgumentException for unknown city names")
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
