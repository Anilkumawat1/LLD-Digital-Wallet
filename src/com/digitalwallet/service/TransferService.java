package com.digitalwallet.service;

import com.digitalwallet.exception.DuplicationRequestException;
import com.digitalwallet.exception.WalletException;
import com.digitalwallet.model.PaymentMethod;
import com.digitalwallet.model.Transaction;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.payment.Payment;
import com.digitalwallet.payment.PaymentFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TransferService {
    private final Map<String, Wallet> wallets = new HashMap<>();
    private final TransactionHistory log = new TransactionHistory();
    private final Set<String> processedKey = Collections.synchronizedSet(new HashSet<>());
    private final PaymentFactory paymentFactory = new PaymentFactory();

    public Wallet createWallet(String userId){
        String id = "W-" + UUID.randomUUID().toString().substring(0,6);
        Wallet wallet = new Wallet(id,userId);
        wallets.put(userId,wallet);
        return wallet;
    }

    public List<Transaction> getHistory(String walletId){
        return log.getTransaction(walletId);
    }

    public BigDecimal getBalance(String walletId){
        return wallets.get(walletId).getBalance();
    }

    public Transaction addFunds(String walletId, BigDecimal amount, PaymentMethod paymentMethod, String idempotencyKey){
        claimKey(idempotencyKey);
        Wallet wallet = wallets.get(walletId);
        if(Objects.isNull(wallet)){
            throw new WalletException("Invalid wallet ID: "+ walletId);
        }
        Payment payment = paymentFactory.getPaymentMethod(paymentMethod);
        if(!payment.addFunds(walletId,amount)){
            throw new RuntimeException("Payment got failed: "+payment.getPaymentMethod());
        }
        ReentrantLock lock = wallet.lock;
        lock.lock();
        try{
            Transaction tnx =  wallet.credit(amount, idempotencyKey, "Pay to wallet", paymentMethod);
            log.appendTransaction(tnx);
            return tnx;
        } finally {
            lock.unlock();
        }
    }

    public Transaction[] transfer(String senderId, String receiverId, BigDecimal amount, String idempotencyId){
        claimKey(idempotencyId);
        Wallet senderWallet = wallets.get(senderId);
        if(Objects.isNull(senderWallet)){
            throw new WalletException("Invalid wallet ID: "+ senderId);
        }
        Wallet receiverWallet = wallets.get(receiverId);
        if(Objects.isNull(receiverWallet)){
            throw new WalletException("Invalid wallet ID: "+ receiverId);
        }
        Wallet first = senderWallet.getId().compareTo(receiverWallet.getId())>=0? senderWallet : receiverWallet;
        Wallet second = first == senderWallet ? receiverWallet : senderWallet;
        first.lock.lock();
        try {
            second.lock.lock();
            try {
                Transaction debitTransaction = senderWallet.debit(amount,idempotencyId,"Transfer to :"+receiverWallet.getUserId());
                log.appendTransaction(debitTransaction);
                Transaction creditTransaction = receiverWallet.credit(amount,idempotencyId,"Receive from :"+senderWallet.getUserId(),PaymentMethod.WALLET);
                log.appendTransaction(creditTransaction);
                return new Transaction[]{debitTransaction,creditTransaction};
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }

    private void claimKey(String idempotencyKey) {
        if(!processedKey.add(idempotencyKey)){
            throw new DuplicationRequestException("Transaction already done duplicate request");
        }
    }
}
