package com.digitalwallet.service;

import com.digitalwallet.model.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionHistory {
    private final List<Transaction> transactionHistoryList = Collections.synchronizedList(new ArrayList<>());

    void appendTransaction(Transaction transaction){
        transactionHistoryList.add(transaction);
    }
    public List<Transaction> getTransaction(String walletId){
        List<Transaction> result = new ArrayList<>();
        for(Transaction transaction : transactionHistoryList){
            if(transaction.walletId().equals(walletId)){
                result.add(transaction);
            }
        }
        return result;
    }
}
