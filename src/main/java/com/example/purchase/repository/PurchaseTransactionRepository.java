package com.example.purchase.repository;

import com.example.purchase.domain.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, UUID> {
    // Standard CRUD methods (including findById) are inherited automatically
}