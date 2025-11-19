package com.example.BankingApplication.Entity;

import com.example.BankingApplication.Entity.Enumeration.*;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "agents")
@Data
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long agentId;

    private String branchName;
    private String businessName;

    @Column(unique = true)
    private String tin;

    private BigDecimal agentBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AgentStatus status = AgentStatus.ACTIVE;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}