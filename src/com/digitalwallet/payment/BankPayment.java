package com.digitalwallet.payment;

import com.digitalwallet.model.PaymentMethod;
import java.math.BigDecimal;

public class BankPayment implements Payment {
    @Override
    public boolean addFunds(String walletId, BigDecimal amount) {
        System.out.printf("[Bank] Charging $%s to wallet %s", amount, walletId);
        return true;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.BANK;
    }
}
