package com.example.deliverydemoapi.exceptions;

public class VehicleForbiddenException extends RuntimeException {

  /**
   * Returns the fixed message describing that the selected vehicle type is forbidden.
   *
   * @return the exception message
   */
  @Override
  public String getMessage() {
    return "Usage of selected vehicle type is forbidden";
  }
}
