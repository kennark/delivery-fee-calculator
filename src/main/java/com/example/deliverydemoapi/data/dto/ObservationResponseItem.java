package com.example.deliverydemoapi.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

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
