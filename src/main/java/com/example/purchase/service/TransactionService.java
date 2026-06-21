package com.example.purchase.service;

import com.example.purchase.client.TreasuryApiClient;
import com.example.purchase.client.TreasuryApiClient.ExchangeRateRecord;
import com.example.purchase.domain.PurchaseTransaction;
import com.example.purchase.dto.ConvertedTransactionResponse;
import com.example.purchase.dto.CreateTransactionRequest;
import com.example.purchase.exception.CurrencyConversionException;
import com.example.purchase.exception.TransactionNotFoundException;
import com.example.purchase.repository.PurchaseTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final PurchaseTransactionRepository repository;
    private final TreasuryApiClient treasuryApiClient;

    public TransactionService(PurchaseTransactionRepository repository, TreasuryApiClient treasuryApiClient) {
        this.repository = repository;
        this.treasuryApiClient = treasuryApiClient;
    }

    /**
     * Requirement #1: Validates and sanitizes incoming transaction amounts to database records.
     */
    @Transactional
    public PurchaseTransaction saveTransaction(CreateTransactionRequest request) {
        // Financial Rigor: Force scale assignment at entry point via standard banking algorithm rules (HALF_UP)
        BigDecimal roundedAmount = request.purchaseAmount().setScale(2, RoundingMode.HALF_UP);
        
        PurchaseTransaction transaction = new PurchaseTransaction(
                request.description(),
                request.transactionDate(),
                roundedAmount
        );
        return repository.save(transaction);
    }

    /**
     * Requirement #2: Handles standard target lookup and historical evaluation workflows.
     */
    @Transactional(readOnly = true)
    public ConvertedTransactionResponse getConvertedTransaction(UUID id, String targetCurrency) {
        // Ensure entity match extraction exists before executing conversion pipelines
        PurchaseTransaction tx = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found for ID: " + id));

        LocalDate purchaseDate = tx.getTransactionDate();
        // Smart Conversion: Establish boundary tracking dates to evaluate exactly 6 months prior
        LocalDate sixMonthsPrior = purchaseDate.minusMonths(6);

        // Fetch targeting array from the remote client layer
        List<ExchangeRateRecord> rates = treasuryApiClient.fetchExchangeRates(targetCurrency, sixMonthsPrior, purchaseDate);

        // Enforce the 6-month historical out-of-bounds rule
        if (rates.isEmpty()) {
            throw new CurrencyConversionException(
                    "The purchase cannot be converted to the target currency. No conversion rate available within 6 months prior to the purchase date."
            );
        }

        // Extrapolate the first record (guaranteed by '-record_date' query sorting logic to be optimal)
        ExchangeRateRecord optimalRate = rates.get(0);
        BigDecimal exchangeRate = optimalRate.exchangeRate();

        // Perform calculation: Multiply original value * exchange rate and scale result to exactly 2 decimals
        BigDecimal convertedAmount = tx.getPurchaseAmountUsd()
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        return new ConvertedTransactionResponse(
                tx.getId(),
                tx.getDescription(),
                tx.getTransactionDate(),
                tx.getPurchaseAmountUsd(),
                targetCurrency,
                exchangeRate,
                convertedAmount
            );
    }
}