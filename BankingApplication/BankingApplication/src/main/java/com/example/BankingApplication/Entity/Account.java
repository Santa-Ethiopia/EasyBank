package com.example.BankingApplication.Entity;

import com.example.BankingApplication.Entity.Enumeration.*;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    private String accountNumber;

    @Column(nullable = false)
    private String firstName;
    private String middleName;
    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate birthDate;

    @Column(unique = true)
    private String nationalId;
    private String city;
    private String subCity;
    private String woreda;
    private String mothersName;
    private String mobileNumber;
    private String email;

    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.PENDING;

    private BigDecimal accountBalance = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Pending modification fields
    private String pendingFirstName;
    private String pendingLastName;
    private String pendingMobileNumber;
    private String pendingEmail;
    private String pendingCity;

    @Enumerated(EnumType.STRING)
    private ModificationStatus modificationStatus = ModificationStatus.NONE;
}