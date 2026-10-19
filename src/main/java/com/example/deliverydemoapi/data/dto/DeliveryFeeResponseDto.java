package com.example.deliverydemoapi.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload containing the calculated delivery fee.
 *
 * @param fee the calculated delivery fee
 */
@Schema(description = "Response payload containing the calculated delivery fee")
public record DeliveryFeeResponseDto(
        @Schema(description = "The calculated delivery fee", example = "3.5")
        double fee
) {
}
