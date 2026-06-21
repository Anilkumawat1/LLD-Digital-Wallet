package com.digitalwallet.payment;

import com.digitalwallet.exception.WalletException;
import com.digitalwallet.model.PaymentMethod;

public class PaymentFactory {
    public Payment getPaymentMethod(PaymentMethod paymentMethod){
        return switch (paymentMethod){
            case CARD -> new CardPayment();
            case UPI ->  new UPIPayment();
            case BANK -> new BankPayment();
            case WALLET -> throw new WalletException("WALLET payment method not supported for adding funds");
        };
    }
}
