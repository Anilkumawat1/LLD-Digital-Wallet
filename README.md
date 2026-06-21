# Digital Wallet — Low-Level Design (LLD)

A production-grade, object-oriented Digital Wallet System built in Java, demonstrating core Low-Level Design (LLD) principles, SOLID design, and multiple design patterns.

📑 Table of Contents
1. [Functional Requirements](#-functional-requirements)
2. [Non-Functional Requirements](#-non-functional-requirements)
3. [Class Diagram](#-class-diagram)
4. [Design Patterns Used & Why](#-design-patterns-used--why)
5. [OOP Concepts Applied](#-oop-concepts-applied)
6. [System Flow — How It Works](#-system-flow--how-it-works)
7. [Project Structure](#-project-structure)
8. [Code Output](#-code-output)
9. [Future Extensibility](#-future-extensibility)
10. [How to Run](#-how-to-run)
11. [Key Design Principles (SOLID)](#-key-design-principles-solid)
12. [Preventing Deadlocks & Lock Contention](#-preventing-deadlocks--lock-contention)

---

## 📋 Functional Requirements
- **Wallet Creation**: Support creating a unique digital wallet for users (`userId`). Each wallet generates a unique ID prefixed with `W-` followed by a random identifier.
- **Add Funds**: Add money to the wallet from multiple sources like UPI, Cards, and Bank Transfer.
- **Wallet Transfer**: Facilitate transaction transfer from one wallet user to another wallet user.
- **Idempotency**: Prevent double operations (double credit or double debit) by checking unique transaction idempotency keys.
- **Transaction History**: Append and view complete credit/debit transaction logs per wallet.
- **Balance Inquiries**: View the current balance of any wallet user.
- **Concurrent Safety**: Support multiple transactions acting on the same wallet concurrently (e.g., wallet transfers and additions) without race conditions.

---

## ⚙️ Non-Functional Requirements
- **High Concurrency & Thread-Safety**: Ensure that multiple transactions occur concurrently without balance corruption.
- **Deadlock Avoidance**: Order acquisition of resource locks strictly to avoid deadlock scenarios during multi-resource operations like wallet transfers.
- **Strict Idempotency**: System must safely reject duplicate request tokens for retry calls.
- **High Extensibility**: Easy to plug in new payment methods without breaking existing logic (Open-Closed Principle).

---

## 🖼️ Class Diagram

*(Space reserved for Class Diagram — to be updated)*

---

## 🛠️ Design Patterns Used & Why

### 1. Strategy Pattern
We define a common interface `Payment` containing `addFunds(String walletId, BigDecimal amount)`. Concrete implementations (`UPIPayment`, `CardPayment`, `BankPayment`) implement different strategies to charge funds from respective external providers. This decouples the client code from external charging mechanisms.

### 2. Simple Factory Pattern
We use `PaymentFactory` to instantiate concrete implementations of `Payment` based on `PaymentMethod` enum. This encapsulates object creation logic, making it easy to introduce new payment sources.

### 3. Lock Ordering (Resource Sorting Pattern)
To safely lock two wallets during a transfer without triggering deadlocks, we determine lock order by comparing wallet IDs using `compareTo()`. This guarantees that locks are always acquired in a consistent lexicographical order.

---

## 💎 OOP Concepts Applied
- **Encapsulation**: Wallet state (`balance`) and locking mechanisms are kept private/protected. They can only be modified through controlled class methods (`credit()`, `debit()`) to ensure invariants are always maintained.
- **Abstraction**: High-level clients utilize the unified `Payment` interface without knowing the internal APIs of payment gateways (UPI, Bank, Card).
- **Polymorphism**: The `Payment` type resolves dynamically at runtime, allowing different implementations of `addFunds(...)` to run seamlessly.
- **Inheritance**: Subclasses of custom exceptions inherit from a common `WalletException` hierarchy (`InsufficientBalanceException`, `DuplicationRequestException`) to simplify application error handling.

---

## 🌊 System Flow — How It Works
1. **Wallet Initialization**: The system registers a new wallet mapping `userId` to a `Wallet` object containing a unique identifier.
2. **Adding Funds**: 
   - Generates/receives an idempotency key.
   - Asserts the key hasn't been claimed before.
   - Fetches the appropriate payment processor strategy from `PaymentFactory` and charges the method.
   - Locks the wallet, executes `credit(...)`, logs the transaction, and returns success.
3. **Wallet Transfer**:
   - Asserts the transfer idempotency key is unique.
   - Resolves the sorted ordering of sender and receiver wallets by ID.
   - Sequentially locks the first wallet, then the second wallet.
   - Debits the sender, credits the receiver, logs both transaction legs, and safely releases the locks in reverse order.

---

## 📂 Project Structure

```text
src/
└── com/
    └── digitalwallet/
        ├── DigitalWallet.java              # Main Application runner / test driver
        ├── exception/
        │   ├── WalletException.java        # Base wallet exception
        │   ├── InsufficientBalanceException.java
        │   └── DuplicationRequestException.java
        ├── model/
        │   ├── Wallet.java                 # Wallet domain model with locks
        │   ├── Transaction.java            # Immutable Transaction record
        │   ├── PaymentMethod.java          # enum: UPI, CARD, BANK, WALLET
        │   ├── PaymentStatus.java          # enum: COMPLETED, FAILED, REFUNDED
        │   └── TransactionType.java        # enum: CREDIT, DEBIT
        ├── payment/
        │   ├── Payment.java                # Interface for funding mechanisms
        │   ├── PaymentFactory.java         # Factory for resolving payment methods
        │   ├── CardPayment.java
        │   ├── UPIPayment.java
        │   └── BankPayment.java
        └── service/
            ├── TransferService.java        # Main operations orchestration service
            └── TransactionHistory.java     # Synchronized memory logger
```

---

## 💻 Code Output
Running the driver class produces the following structured log console output:

```text
=== 1. Creating Wallets ===
Alice's Wallet: ID = W-61f7de, User = alice, Balance = $0
Bob's Wallet: ID = W-2ed684, User = bob, Balance = $0

=== 2. Adding Funds ===
Adding $100.00 to Alice's wallet using CARD...
[Card] Charging $100.00 to wallet alice -> Success: Transaction[id=de5b0339, walletId=W-61f7de, amount=100.00, transactionType=CREDIT, paymentStatus=COMPLETED, paymentMethod=CARD, idempotencyKey=idem-key-1, description=Pay to wallet, timeStamp=2026-06-21T23:59:06.096010]

Adding $50.00 to Bob's wallet using UPI...
[UPI] Charging $50.00 to wallet bob -> Success: Transaction[id=c4ceb705, walletId=W-2ed684, amount=50.00, transactionType=CREDIT, paymentStatus=COMPLETED, paymentMethod=UPI, idempotencyKey=idem-key-2, description=Pay to wallet, timeStamp=2026-06-21T23:59:06.107810]
Alice's Balance: $100.00
Bob's Balance: $50.00

=== 3. Testing Idempotency (Duplicate Fund Addition) ===
Attempting to add $100.00 to Alice's wallet with the same idempotency key 'idem-key-1'...
 -> Caught expected exception: Transaction already done duplicate request

=== 4. Transferring Funds (Alice -> Bob) ===
Transferring $40.00 from Alice to Bob...
 -> Debit transaction: Transaction[id=b1408754, walletId=W-61f7de, amount=40.00, transactionType=DEBIT, paymentStatus=COMPLETED, paymentMethod=WALLET, idempotencyKey=transfer-key-1, description=Transfer to :bob, timeStamp=2026-06-21T23:59:06.108048]
 -> Credit transaction: Transaction[id=2338a991, walletId=W-2ed684, amount=40.00, transactionType=CREDIT, paymentStatus=COMPLETED, paymentMethod=WALLET, idempotencyKey=transfer-key-1, description=Receive from :alice, timeStamp=2026-06-21T23:59:06.108100]
Alice's Balance: $60.00
Bob's Balance: $90.00

=== 5. Testing Idempotency on Transfer (Duplicate Transfer) ===
Attempting to transfer $40.00 from Alice to Bob with duplicate key 'transfer-key-1'...
 -> Caught expected exception: Transaction already done duplicate request

=== 6. Testing Insufficient Balance ===
Attempting to transfer $100.00 from Alice to Bob (Alice's balance is lower)...
 -> Caught expected exception: Insufficient balance

=== 7. Testing Invalid Wallet ID Exception ===
Attempting to transfer from non-existent user 'charlie' to Bob...
 -> Caught expected exception: Invalid wallet ID: charlie

=== 8. Print Transaction History ===
Alice's Transaction History (Wallet ID: W-61f7de):
 - Transaction[id=de5b0339, walletId=W-61f7de, amount=100.00, transactionType=CREDIT, paymentStatus=COMPLETED, paymentMethod=CARD, idempotencyKey=idem-key-1, description=Pay to wallet, timeStamp=2026-06-21T23:59:06.096010]
 - Transaction[id=b1408754, walletId=W-61f7de, amount=40.00, transactionType=DEBIT, paymentStatus=COMPLETED, paymentMethod=WALLET, idempotencyKey=transfer-key-1, description=Transfer to :bob, timeStamp=2026-06-21T23:59:06.108048]

Bob's Transaction History (Wallet ID: W-2ed684):
 - Transaction[id=c4ceb705, walletId=W-2ed684, amount=50.00, transactionType=CREDIT, paymentStatus=COMPLETED, paymentMethod=UPI, idempotencyKey=idem-key-2, description=Pay to wallet, timeStamp=2026-06-21T23:59:06.107810]
 - Transaction[id=2338a991, walletId=W-2ed684, amount=40.00, transactionType=CREDIT, paymentStatus=COMPLETED, paymentMethod=WALLET, idempotencyKey=transfer-key-1, description=Receive from :alice, timeStamp=2026-06-21T23:59:06.108100]
```

---

## 🚀 Future Extensibility
- **Refund Orchestration**: Support transaction state transitions (`REFUNDED`) and automatic reconciliation.
- **Daily Transaction Capping**: Implement limits on total transfers or credits per 24-hour cycle.
- **State management**: Add wallet status types (`ACTIVE`, `BLOCKED`, `SUSPENDED`) to restrict transfers on flagged accounts.
- **Database Persistence**: Transition from internal memory structures (`HashMap`, `synchronizedSet`) to a persistent database (e.g. PostgreSQL) using ACID transactions.

---

## 🏃 How to Run
Compile and run directly from root directory:
```bash
# Compile all source files
javac -d out src/com/digitalwallet/**/*.java

# Run test cases
java -cp out com.digitalwallet.DigitalWallet
```

---

## 📐 Key Design Principles (SOLID)

- **Single Responsibility Principle (SRP)**: Each class has a single focused job. `Wallet` handles state logic (credit/debit), `TransferService` coordinates wallet transitions and idempotency check, `PaymentFactory` handles strategy instantiation, and `TransactionHistory` performs transaction logging.
- **Open-Closed Principle (OCP)**: The system is easily open for extension but closed for modification. If a new payment type (e.g., NetBanking) is required, we can create a class `NetBankingPayment` implementing `Payment` and map it in the enum/factory, without modifying existing payment processors.
- **Liskov Substitution Principle (LSP)**: `CardPayment`, `UPIPayment`, and `BankPayment` can be substituted for the `Payment` interface anywhere without affecting the correctness of the execution.
- **Interface Segregation Principle (ISP)**: The `Payment` interface exposes only the relevant behavior required by the orchestration services (`addFunds` and `getPaymentMethod`), keeping clients clean from bloated dependencies.
- **Dependency Inversion Principle (DIP)**: `TransferService` depends on the abstract `Payment` interface rather than concrete processor classes, delegating resolution dynamically.

---

## 🔒 Preventing Deadlocks & Lock Contention

### The Deadlock Risk
In concurrent multi-threaded environments, deadlocks can occur when two transactions try to transfer money between the same two accounts in reverse directions simultaneously:
- **Thread 1** tries to transfer from **Alice** to **Bob**. It locks Alice's wallet, then tries to lock Bob's wallet.
- **Thread 2** tries to transfer from **Bob** to **Alice**. It locks Bob's wallet, then tries to lock Alice's wallet.
- This creates a **Circular Wait** condition: Thread 1 holds Alice and waits for Bob; Thread 2 holds Bob and waits for Alice. Both block forever.

### The Solution: Global Lock Ordering
We eliminate the Circular Wait condition by establishing a strict global lock acquisition order. 
Instead of acquiring locks based on the roles of `sender` and `receiver`, we compare the unique, immutable wallet IDs (`W-xxxxx`) using Java's `String.compareTo()`:

```java
Wallet first = senderWallet.getId().compareTo(receiverWallet.getId()) >= 0 ? senderWallet : receiverWallet;
Wallet second = first == senderWallet ? receiverWallet : senderWallet;

first.lock.lock();
try {
    second.lock.lock();
    try {
        // Perform transfer logic safely
    } finally {
        second.lock.unlock();
    }
} finally {
    first.lock.unlock();
}
```

Since the wallet IDs are unique and immutable, **Thread 1** and **Thread 2** will always agree on which wallet has the "smaller" ID and will lock that wallet first. This guarantees a sequential lock acquisition chain, preventing deadlocks entirely.
