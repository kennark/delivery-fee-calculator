package com.example.deliverydemoapi.data.enums;

import lombok.Getter;

public enum City {
    TALLINN("Tallinn-Harku"),
    TARTU("Tartu-Tõravere"),
    PÄRNU("Pärnu");

    @Getter
    private final String stationName;

    City(String stationName) {
        this.stationName = stationName;
    }
}
