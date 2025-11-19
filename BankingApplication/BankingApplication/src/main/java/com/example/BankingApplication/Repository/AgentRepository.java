package com.example.BankingApplication.Repository;

import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByTin(String tin);
    Optional<Agent> findByUserUserId(Long userId);
    List<Agent> findByStatus(AgentStatus status);
}