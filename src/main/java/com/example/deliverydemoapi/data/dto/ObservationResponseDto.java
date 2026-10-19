package com.example.deliverydemoapi.data.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Response payload representing the weather observation API result.
 *
 * @param timestamp the observation timestamp as a Unix epoch second
 * @param stations  the list of weather stations included in the observation
 */
public record ObservationResponseDto(
        @JacksonXmlProperty(isAttribute = true)
        Long timestamp,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "station")
        List<ObservationResponseItem> stations
) { }