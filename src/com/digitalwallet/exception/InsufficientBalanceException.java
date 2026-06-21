package com.digitalwallet.exception;

public class InsufficientBalanceException extends WalletException {
    public InsufficientBalanceException(String msg) {
        super(msg);
    }
}
