package com.example.BankingApplication.Service;

import com.example.BankingApplication.Entity.Account;
import com.example.BankingApplication.Entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");


    public void sendAccountCreationEmail(String toEmail, String username, String plainPassword, Account account) {
        String subject = "Welcome to Our Bank - Your Account Has Been Created";
        String emailContent = """
            Dear %s %s,
            
            Your bank account has been successfully created!
            
            Account Details:
            - Account Number: %s
            - Username: %s
            - Temporary Password: %s
            - Registration Date: %s
            
            Please change your password after first login.
            
            Best regards,
            EasyBank
            """.formatted(
                account.getFirstName(),
                account.getLastName(),
                account.getAccountNumber(),
                username,
                plainPassword,
                account.getRegistrationDate().format(DATE_FORMATTER)
        );

        sendSimpleEmail(toEmail, subject, emailContent);
    }


    public void sendTransactionNotification(Transaction transaction, String customerEmail) {
        String transactionType = transaction.getTransactionType().toString();
        String amount = formatCurrency(transaction.getAmount());
        String balance = formatCurrency(transaction.getAccountBalance());
        String accountNumber = transaction.getAccount().getAccountNumber();

        String subject = "Transaction Notification - " + transactionType;
        String emailContent;

        if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
            emailContent = """
                Dear Customer,
                
                Your account '%s' has been credited with ETB %s. 
                Your current balance is ETB %s.
                
                Thank You for banking with us.
                Best regards,
                  EasyBank
                """.formatted(accountNumber, amount, balance);
        } else if ("WITHDRAWAL".equalsIgnoreCase(transactionType)) {
            emailContent = """
                Dear Customer,
                
                Your account '%s' has been debited with ETB %s. 
                Your current balance is ETB %s.
                
                Thank You for banking with us.
                Best regards,
                  EasyBank
                """.formatted(accountNumber, amount, balance);
        } else {

            emailContent = """
                Dear Customer,
                
                Transaction Type: %s
                Account: %s
                Amount: ETB %s
                Current Balance: ETB %s
                Date: %s
                
                Thank You for banking with us.
                """.formatted(
                    transactionType,
                    accountNumber,
                    amount,
                    balance,
                    transaction.getTransactionDate().format(DATE_FORMATTER)
            );
        }

        sendSimpleEmail(customerEmail, subject, emailContent);
    }


    public void sendAccountModificationApprovalEmail(String toEmail, Account account) {
        String subject = "Account Modification Approved";
        String emailContent = """
            Dear %s %s,
            
            Your account modification request has been approved.
            
            Account: %s
            
            Best regards,
            EasyBank
            """.formatted(
                account.getFirstName(),
                account.getLastName(),
                account.getAccountNumber()
        );

        sendSimpleEmail(toEmail, subject, emailContent);
    }


    public void sendAccountModificationRejectionEmail(String toEmail, Account account, String reason) {
        String subject = "Account Modification Request Update";
        String emailContent = """
            Dear %s %s,
            
            Your account modification request could not be processed.
            
            Reason: %s
            Account: %s
            
            Please contact customer support for more information.
            
            Best regards,
            EasyBank
            """.formatted(
                account.getFirstName(),
                account.getMiddleName(),
                reason != null ? reason : "Please contact customer support",
                account.getAccountNumber()
        );

        sendSimpleEmail(toEmail, subject, emailContent);
    }


    private void sendSimpleEmail(String to, String subject, String text) {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(2).toString();
    }
}