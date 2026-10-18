package com.example.deliverydemoapi.data.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public record ObservationResponseDto(
        @JacksonXmlProperty(isAttribute = true)
        Long timestamp,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "station")
        List<ObservationResponseItem> stations
) { }


