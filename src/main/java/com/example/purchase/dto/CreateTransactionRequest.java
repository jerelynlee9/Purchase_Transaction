package com.example.purchase.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable API input validation container mapping incoming transaction models.
 */
public record CreateTransactionRequest(
    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    String description,

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    LocalDate transactionDate,

    @NotNull(message = "Purchase amount is required")
    @Positive(message = "Purchase amount must be a positive value")
    BigDecimal purchaseAmount
) {}