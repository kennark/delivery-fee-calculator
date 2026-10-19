package com.example.deliverydemoapi.data.converters;

import com.example.deliverydemoapi.data.enums.City;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToCityConverter implements Converter<String, City> {

    /**
     * Allows to convert String to City enum with non-uppercase String.
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     * @return Enum of type City
     */
    @Override
    public City convert(String source) {
        return City.valueOf(source.toUpperCase().trim());
    }
}
