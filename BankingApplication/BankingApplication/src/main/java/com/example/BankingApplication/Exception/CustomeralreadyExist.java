package com.example.BankingApplication.Exception;

public class CustomeralreadyExist extends RuntimeException {
    public CustomeralreadyExist(String message) {
        super(message);
    }
}
