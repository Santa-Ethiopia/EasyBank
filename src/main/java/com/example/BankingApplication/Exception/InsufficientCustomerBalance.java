package com.example.BankingApplication.Exception;

public class InsufficientCustomerBalance extends RuntimeException {
    public InsufficientCustomerBalance(String message) {
        super(message);
    }
}
