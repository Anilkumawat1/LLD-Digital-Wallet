package com.digitalwallet.payment;

import com.digitalwallet.model.PaymentMethod;
import java.math.BigDecimal;

public interface Payment {
    boolean addFunds(String walletId, BigDecimal amount);
    PaymentMethod getPaymentMethod();
}
