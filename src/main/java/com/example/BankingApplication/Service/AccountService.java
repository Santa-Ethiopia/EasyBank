package com.example.BankingApplication.Service;

import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import com.example.BankingApplication.Exception.*;
import com.example.BankingApplication.Repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.time.Period;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AgentRepository agentRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserUserId(userId);
    }

    public Account createPendingAccount(Account account) {

        validateAge(account.getBirthDate());
        if (isNationalIdExists(account.getNationalId())) {
            Optional<Account> existingAccount = accountRepository.findByNationalId(account.getNationalId());
            String theAccount = existingAccount.get().getAccountNumber();
            String maskedAccountNumber = maskAccountNumber(theAccount);
            throw new CustomeralreadyExist("National ID " + account.getNationalId() +
                    " is already registered. Account Number: " + maskedAccountNumber);
        }
        account.setAccountNumber(generateAccountNumber());
        account.setRegistrationDate(LocalDateTime.now());
        account.setStatus(AccountStatus.PENDING);
        return accountRepository.save(account);

    }
    public void validateAge(LocalDate birthDate) {
        if (birthDate != null) {
            LocalDate today = LocalDate.now();
            Period age = Period.between(birthDate, today);
            if (age.getYears() < 18) {
                throw new UnderAgeException("Account holder must be at least 18 years old");
            }
        }
    }
    public boolean isNationalIdExists(String nationalId) {
        return accountRepository.existsByNationalId(nationalId);
    }
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        int length = accountNumber.length();
        return "*".repeat(length - 4) + accountNumber.substring(length - 4);
    }

    public Account approveAccountAndCreateUser(String accountNumber, String username, String password, Long agentId) {
        logger.info("=== DEBUG: Starting account approval ===");
        logger.info("Account: {}, Username: {}, Agent: {}", accountNumber, username, agentId);

        try {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

            logger.info("=== DEBUG: Found account ===");

            String encodedPassword = passwordEncoder.encode(password);
            User user = userService.createUser(username, encodedPassword, UserRole.ROLE_CUSTOMER);

            logger.info("=== DEBUG: Created user ===");
            account.setUser(user);
            account.setStatus(AccountStatus.ACTIVE);
            Account savedAccount = accountRepository.save(account);

            logger.info("=== DEBUG: Account saved successfully ===");
            try {
                emailService.sendAccountCreationEmail(account.getEmail(), username, password, savedAccount);
                logger.info("=== DEBUG: Email sent successfully ===");
            } catch (Exception e) {
                logger.error("=== DEBUG: Email failed but continuing: {} ===", e.getMessage());
            }

            logger.info("=== DEBUG: Account approval COMPLETED ===");
            return savedAccount;

        } catch (Exception e) {
            logger.error("=== DEBUG: Error in account approval: {} ===", e.getMessage());
            throw e;
        }
    }

    public Transaction agentDeposit(String accountNumber, BigDecimal amount, Long agentId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent not found"));

        if (agent.getAgentBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Agent has insufficient balance. Available: $" + agent.getAgentBalance());
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Deposit amount must be greater than zero");
        }

        agent.setAgentBalance(agent.getAgentBalance().subtract(amount));
        account.setAccountBalance(account.getAccountBalance().add(amount));

        agentRepository.save(agent);
        accountRepository.save(account);

        Transaction transaction = createTransaction(account, TransactionType.DEPOSIT, amount, "Agent-" + agentId);
        try {
            emailService.sendTransactionNotification(transaction, account.getEmail());
            logger.info("Deposit notification email sent for account: {}", accountNumber);
        } catch (Exception e) {
            logger.error("Failed to send deposit notification email for account: {}", accountNumber, e);
        }

        return transaction;
    }

    public Transaction agentWithdraw(String accountNumber, BigDecimal amount, Long agentId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent not found"));

        if (account.getAccountBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Customer has insufficient balance.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Withdrawal amount must be greater than zero");
        }
        account.setAccountBalance(account.getAccountBalance().subtract(amount));
        agent.setAgentBalance(agent.getAgentBalance().add(amount));

        accountRepository.save(account);
        agentRepository.save(agent);

        Transaction transaction = createTransaction(account, TransactionType.WITHDRAWAL, amount, "Agent-" + agentId);
        try {
            emailService.sendTransactionNotification(transaction, account.getEmail());
            logger.info("Withdrawal notification email sent for account: {}", accountNumber);
        } catch (Exception e) {
            logger.error("Failed to send withdrawal notification email for account: {}", accountNumber, e);
        }

        return transaction;
    }

    public Transaction transferBetweenAccounts(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String performedBy) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new CustomerAccountNotFoundException("From account not found: " + fromAccountNumber));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new CustomerAccountNotFoundException("Credit account not found: " + toAccountNumber));

        if (fromAccount.getAccountBalance().compareTo(amount) < 0) {
            throw new InsufficientCustomerBalance("Insufficient balance for transfer. Available: $" + fromAccount.getAccountBalance());
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Transfer amount must be greater than zero");
        }

        fromAccount.setAccountBalance(fromAccount.getAccountBalance().subtract(amount));
        toAccount.setAccountBalance(toAccount.getAccountBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        createTransaction(fromAccount, TransactionType.TRANSFER, amount.negate(), performedBy);
        return createTransaction(toAccount, TransactionType.TRANSFER, amount, performedBy);
    }

    public Account requestAccountModification(String accountNumber, Account modifiedAccount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.setPendingFirstName(modifiedAccount.getFirstName());
        account.setPendingLastName(modifiedAccount.getLastName());
        account.setPendingMobileNumber(modifiedAccount.getMobileNumber());
        account.setPendingEmail(modifiedAccount.getEmail());
        account.setPendingCity(modifiedAccount.getCity());
        account.setModificationStatus(ModificationStatus.PENDING);

        return accountRepository.save(account);
    }

    public Account approveAccountModification(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        // Apply pending modifications
        account.setFirstName(account.getPendingFirstName());
        account.setLastName(account.getPendingLastName());
        account.setMobileNumber(account.getPendingMobileNumber());
        account.setEmail(account.getPendingEmail());
        account.setCity(account.getPendingCity());

        // Clear pending fields
        account.setPendingFirstName(null);
        account.setPendingLastName(null);
        account.setPendingMobileNumber(null);
        account.setPendingEmail(null);
        account.setPendingCity(null);
        account.setModificationStatus(ModificationStatus.NONE);

        Account savedAccount = accountRepository.save(account);

        try {
            emailService.sendAccountModificationApprovalEmail(account.getEmail(), savedAccount);
            logger.info("Account modification approval email sent for account: {}", accountNumber);
        } catch (Exception e) {
            logger.error("Failed to send modification approval email for account: {}", accountNumber, e);
        }

        return savedAccount;
    }

    public void rejectAccountModification(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.setPendingFirstName(null);
        account.setPendingLastName(null);
        account.setPendingMobileNumber(null);
        account.setPendingEmail(null);
        account.setPendingCity(null);
        account.setModificationStatus(ModificationStatus.REJECTED);

        accountRepository.save(account);
        try {
            emailService.sendAccountModificationRejectionEmail(account.getEmail(), account, "Modification request was rejected");
            logger.info("Account modification rejection email sent for account: {}", accountNumber);
        } catch (Exception e) {
            logger.error("Failed to send modification rejection email for account: {}", accountNumber, e);
        }
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionRepository.findByAccountAccountNumberOrderByTransactionDateDesc(accountNumber);
    }

    public List<Account> getPendingAccounts() {
        return accountRepository.findByStatus(AccountStatus.PENDING);
    }

    public List<Account> getPendingModifications() {
        return accountRepository.findByModificationStatus(ModificationStatus.PENDING);
    }

    private Transaction createTransaction(Account account, TransactionType type, BigDecimal amount, String performedBy) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setAccountBalance(account.getAccountBalance());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPerformedBy(performedBy);

        return transactionRepository.save(transaction);
    }

    private String generateAccountNumber() {

        int randomDigits = (int)(Math.random() * 1_000_0000); // 7 digits
        String formattedDigits = String.format("%07d", randomDigits);

        return "003" + formattedDigits;
    }
}