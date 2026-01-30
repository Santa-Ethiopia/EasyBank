package com.example.BankingApplication.Entity;

import com.example.BankingApplication.Entity.Enumeration.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private UserRole userRole;

    public Role(UserRole userRole) {
        this.userRole = userRole;
    }
}