package com.example.BankingApplication.Exception;

public class UserNotFoundException extends BankingException {
  public UserNotFoundException(String message) {
    super(message);
  }
}