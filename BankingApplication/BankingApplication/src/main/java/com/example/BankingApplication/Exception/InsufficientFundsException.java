package com.example.BankingApplication.Exception;

public class InsufficientFundsException extends BankingException {
  public InsufficientFundsException(String message) {
    super(message);
  }
}
