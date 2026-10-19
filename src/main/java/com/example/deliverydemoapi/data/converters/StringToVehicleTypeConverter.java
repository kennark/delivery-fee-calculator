package com.example.deliverydemoapi.data.converters;

import com.example.deliverydemoapi.data.enums.VehicleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToVehicleTypeConverter implements Converter<String, VehicleType> {

    /**
     * Allows to convert String to VehicleType enum with non-uppercase String.
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     * @return Enum of type VehicleType
     */
    @Override
    public VehicleType convert(String source) {
        return VehicleType.valueOf(source.toUpperCase().trim());
    }
}
