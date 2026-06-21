package com.digitalwallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(String id, String walletId, BigDecimal amount,
                          TransactionType transactionType, PaymentStatus paymentStatus,
                          PaymentMethod paymentMethod, String idempotencyKey, String description, LocalDateTime timeStamp) {

}
