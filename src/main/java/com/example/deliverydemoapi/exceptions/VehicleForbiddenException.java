package com.example.deliverydemoapi.exceptions;

public class VehicleForbiddenException extends RuntimeException {

  @Override
  public String getMessage() {
    return "Usage of selected vehicle type is forbidden";
  }
}
