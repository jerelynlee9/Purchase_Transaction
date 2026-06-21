package com.example.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Flattened response DTO layout output structures following conversion executions.
 */
public record ConvertedTransactionResponse(
    UUID id,
    String description,
    LocalDate transactionDate,
    BigDecimal originalAmountUsd,
    String targetCurrency,
    BigDecimal exchangeRate,
    BigDecimal convertedAmount
) {}