package com.example.purchase.controller;

import com.example.purchase.domain.PurchaseTransaction;
import com.example.purchase.dto.ConvertedTransactionResponse;
import com.example.purchase.dto.CreateTransactionRequest;
import com.example.purchase.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    /**
     * Endpoint routing handler mapping POST requests to trigger persistence workflows.
     */
    @PostMapping
    public ResponseEntity<PurchaseTransaction> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        PurchaseTransaction saved = service.saveTransaction(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Endpoint routing handler mapping GET queries to process currency conversions.
     */
    @GetMapping("/{id}/convert")
    public ResponseEntity<ConvertedTransactionResponse> getConverted(
            @PathVariable UUID id,
            @RequestParam String targetCurrency) {
        return ResponseEntity.ok(service.getConvertedTransaction(id, targetCurrency));
    }
}