package com.digitalwallet.model;

import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.exception.WalletException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class Wallet {
    private String id;
    private String userId;
    private BigDecimal balance = BigDecimal.ZERO;
    public final ReentrantLock lock = new ReentrantLock();

    public Wallet(String id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Transaction credit(BigDecimal amount, String key, String description, PaymentMethod paymentMethod) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("Credit amount should be positive");
        }
        balance = balance.add(amount);
        return new Transaction(UUID.randomUUID().toString().substring(0, 8), id, amount,
                TransactionType.CREDIT, PaymentStatus.COMPLETED, paymentMethod, key, description, LocalDateTime.now()
        );
    }

    public Transaction debit(BigDecimal amount, String key, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("Debit amount should be positive");
        }
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        balance = balance.subtract(amount);
        return new Transaction(UUID.randomUUID().toString().substring(0, 8), id, amount,
                TransactionType.DEBIT, PaymentStatus.COMPLETED, PaymentMethod.WALLET, key, description, LocalDateTime.now()
        );
    }
}
