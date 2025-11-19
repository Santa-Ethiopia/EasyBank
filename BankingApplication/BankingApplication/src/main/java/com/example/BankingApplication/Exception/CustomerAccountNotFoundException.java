package com.example.BankingApplication.Exception;

public class CustomerAccountNotFoundException extends RuntimeException {
  public CustomerAccountNotFoundException(String message) {
    super(message);
  }
}
