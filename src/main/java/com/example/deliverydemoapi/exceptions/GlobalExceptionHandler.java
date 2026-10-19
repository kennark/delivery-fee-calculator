package com.example.deliverydemoapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link VehicleForbiddenException} by returning its message with a 403 Forbidden status.
     *
     * @param e the thrown exception
     * @return the exception message
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(VehicleForbiddenException.class)
    public String handleVehicleForbiddenException(VehicleForbiddenException e) {
        return e.getMessage();
    }

    /**
     * Handles {@link NoSuchElementException} by returning its message with a 404 Not Found status.
     *
     * @param e the thrown exception
     * @return the exception message
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException e) {
        return e.getMessage();
    }

    /**
     * Handles {@link IllegalArgumentException} by returning its message with a 400 Bad Request status.
     *
     * @param e the thrown exception
     * @return the exception message
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        return e.getMessage();
    }
}
