package com.digitalwallet;

import com.digitalwallet.exception.DuplicationRequestException;
import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.exception.WalletException;
import com.digitalwallet.model.PaymentMethod;
import com.digitalwallet.model.Transaction;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.service.TransferService;

import java.math.BigDecimal;
import java.util.List;

public class DigitalWallet {
    public static void main(String args[]){
        TransferService transferService = new TransferService();

        System.out.println("=== 1. Creating Wallets ===");
        Wallet aliceWallet = transferService.createWallet("alice");
        Wallet bobWallet = transferService.createWallet("bob");
        System.out.println("Alice's Wallet: ID = " + aliceWallet.getId() + ", User = " + aliceWallet.getUserId() + ", Balance = $" + aliceWallet.getBalance());
        System.out.println("Bob's Wallet: ID = " + bobWallet.getId() + ", User = " + bobWallet.getUserId() + ", Balance = $" + bobWallet.getBalance());
        System.out.println();

        System.out.println("=== 2. Adding Funds ===");
        try {
            System.out.println("Adding $100.00 to Alice's wallet using CARD...");
            Transaction t1 = transferService.addFunds("alice", new BigDecimal("100.00"), PaymentMethod.CARD, "idem-key-1");
            System.out.println(" -> Success: " + t1);

            System.out.println("\nAdding $50.00 to Bob's wallet using UPI...");
            Transaction t2 = transferService.addFunds("bob", new BigDecimal("50.00"), PaymentMethod.UPI, "idem-key-2");
            System.out.println(" -> Success: " + t2);
        } catch (Exception e) {
            System.out.println("Error adding funds: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Alice's Balance: $" + transferService.getBalance("alice"));
        System.out.println("Bob's Balance: $" + transferService.getBalance("bob"));
        System.out.println();

        System.out.println("=== 3. Testing Idempotency (Duplicate Fund Addition) ===");
        try {
            System.out.println("Attempting to add $100.00 to Alice's wallet with the same idempotency key 'idem-key-1'...");
            transferService.addFunds("alice", new BigDecimal("100.00"), PaymentMethod.CARD, "idem-key-1");
            System.out.println(" -> Error: Duplicate request was allowed!");
        } catch (DuplicationRequestException e) {
            System.out.println(" -> Caught expected exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" -> Caught unexpected exception: " + e);
        }
        System.out.println();

        System.out.println("=== 4. Transferring Funds (Alice -> Bob) ===");
        try {
            System.out.println("Transferring $40.00 from Alice to Bob...");
            Transaction[] txs = transferService.transfer("alice", "bob", new BigDecimal("40.00"), "transfer-key-1");
            System.out.println(" -> Debit transaction: " + txs[0]);
            System.out.println(" -> Credit transaction: " + txs[1]);
        } catch (Exception e) {
            System.out.println("Error transferring funds: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Alice's Balance: $" + transferService.getBalance("alice"));
        System.out.println("Bob's Balance: $" + transferService.getBalance("bob"));
        System.out.println();

        System.out.println("=== 5. Testing Idempotency on Transfer (Duplicate Transfer) ===");
        try {
            System.out.println("Attempting to transfer $40.00 from Alice to Bob with duplicate key 'transfer-key-1'...");
            transferService.transfer("alice", "bob", new BigDecimal("40.00"), "transfer-key-1");
            System.out.println(" -> Error: Duplicate transfer was allowed!");
        } catch (DuplicationRequestException e) {
            System.out.println(" -> Caught expected exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" -> Caught unexpected exception: " + e);
        }
        System.out.println();

        System.out.println("=== 6. Testing Insufficient Balance ===");
        try {
            System.out.println("Attempting to transfer $100.00 from Alice to Bob (Alice's balance is lower)...");
            transferService.transfer("alice", "bob", new BigDecimal("100.00"), "transfer-key-2");
            System.out.println(" -> Error: Transfer allowed despite insufficient balance!");
        } catch (InsufficientBalanceException e) {
            System.out.println(" -> Caught expected exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" -> Caught unexpected exception: " + e);
        }
        System.out.println();

        System.out.println("=== 7. Testing Invalid Wallet ID Exception ===");
        try {
            System.out.println("Attempting to transfer from non-existent user 'charlie' to Bob...");
            transferService.transfer("charlie", "bob", new BigDecimal("10.00"), "transfer-key-3");
            System.out.println(" -> Error: Allowed transfer from invalid wallet!");
        } catch (WalletException e) {
            System.out.println(" -> Caught expected exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" -> Caught unexpected exception: " + e);
        }
        System.out.println();

        System.out.println("=== 8. Print Transaction History ===");
        System.out.println("Alice's Transaction History (Wallet ID: " + aliceWallet.getId() + "):");
        List<Transaction> aliceHistory = transferService.getHistory(aliceWallet.getId());
        for (Transaction t : aliceHistory) {
            System.out.println(" - " + t);
        }

        System.out.println("\nBob's Transaction History (Wallet ID: " + bobWallet.getId() + "):");
        List<Transaction> bobHistory = transferService.getHistory(bobWallet.getId());
        for (Transaction t : bobHistory) {
            System.out.println(" - " + t);
        }
    }
}
