package com.example.purchase.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase_transactions")
public class PurchaseTransaction {

    // Unique Identifier requirement: Uses random UUID strings to block ID scanning attacks
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Requirement #1: Strict length checking caps strings at 50 chars max inside DB layer
    @Column(nullable = false, length = 50)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    // Financial Rigor: Forces exact scale mapping to guarantee storage precision
    @Column(name = "purchase_amount_usd", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchaseAmountUsd;

    // Default No-Arg Constructor required by JPA Specification
    public PurchaseTransaction() {}

    public PurchaseTransaction(String description, LocalDate transactionDate, BigDecimal purchaseAmountUsd) {
        this.description = description;
        this.transactionDate = transactionDate;
        this.purchaseAmountUsd = purchaseAmountUsd;
    }

    // Standard POJO Accessor and Mutator methods
    public UUID getId() { return id; }
    public String getDescription() { return description; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public BigDecimal getPurchaseAmountUsd() { return purchaseAmountUsd; }

    public void setId(UUID id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public void setPurchaseAmountUsd(BigDecimal purchaseAmountUsd) { this.purchaseAmountUsd = purchaseAmountUsd; }
}