package com.example.BankingApplication.Service;

import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import com.example.BankingApplication.Exception.*;
import com.example.BankingApplication.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public Agent createAgent(Agent agent, String username, String password) {

        if (userService.findByUsername(username).isPresent()) {
            throw new BankingException("Username already exists: " + username);
        }

        if (agentRepository.findByTin(agent.getTin()).isPresent()) {
            throw new BankingException("TIN already exists: " + agent.getTin());
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = userService.createUser(username, encodedPassword, UserRole.ROLE_AGENT);
        agent.setUser(user);
        return agentRepository.save(agent);
    }

    public void depositToAgentBalance(Long agentId, BigDecimal amount) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent not found with ID: " + agentId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Deposit amount must be greater than zero");
        }

        agent.setAgentBalance(agent.getAgentBalance().add(amount));
        agentRepository.save(agent);
    }

    public void deactivateAgent(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent not found with ID: " + agentId));

        agent.setStatus(AgentStatus.INACTIVE);
        agentRepository.save(agent);

        userService.deactivateUser(agent.getUser().getUserId());
    }

    public void activateAgent(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent not found with ID: " + agentId));

        agent.setStatus(AgentStatus.ACTIVE);
        agentRepository.save(agent);

        userService.activateUser(agent.getUser().getUserId());
    }

    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    public List<Agent> getActiveAgents() {
        return agentRepository.findByStatus(AgentStatus.ACTIVE);
    }

    public Optional<Agent> getAgentByUserId(Long userId) {
        return agentRepository.findByUserUserId(userId);
    }
}