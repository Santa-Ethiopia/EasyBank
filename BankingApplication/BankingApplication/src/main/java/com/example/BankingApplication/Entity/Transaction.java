package com.example.BankingApplication.Entity;

import com.example.BankingApplication.Entity.Enumeration.*;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "account_number")
    private Account account;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;
    private BigDecimal accountBalance;
    private LocalDateTime transactionDate;
    private String performedBy;
}