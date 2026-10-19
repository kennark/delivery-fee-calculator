package com.example.deliverydemoapi.data.dto;

/**
 * Response payload containing the calculated delivery fee.
 *
 * @param fee the calculated delivery fee
 */
public record DeliveryFeeResponseDto(double fee) { }
