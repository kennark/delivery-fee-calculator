package com.example.deliverydemoapi.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Represents a single weather station observation returned by the weather API.
 *
 * @param name        the station name
 * @param wmoCode     the WMO code of the station
 * @param temperature the air temperature in degrees Celsius
 * @param windSpeed   the wind speed in metres per second
 * @param phenomenon  the weather phenomenon description
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ObservationResponseItem(
        @JacksonXmlProperty(localName = "name")
        String name,

        @JacksonXmlProperty(localName = "wmocode")
        String wmoCode,

        @JacksonXmlProperty(localName = "airtemperature")
        Double temperature,

        @JacksonXmlProperty(localName = "windspeed")
        Double windSpeed,

        @JacksonXmlProperty(localName = "phenomenon")
        String phenomenon
) { }
