package com.example.BankingApplication.Controllers;

import com.example.BankingApplication.DTO.*;
import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {
    private final AccountService accountService;
    private final AgentService agentService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            Optional<Agent> agent = agentService.getAgentByUserId(user.get().getUserId());
            agent.ifPresent(a -> model.addAttribute("agent", a));
        }

        model.addAttribute("pendingAccounts", accountService.getPendingAccounts());
        model.addAttribute("pendingModifications", accountService.getPendingModifications());
        model.addAttribute("approvalRequest", new AccountApprovalRequest());
        model.addAttribute("transactionRequest", new TransactionRequest());

        return "agent/dashboard";
    }

    @PostMapping("/accounts/approve")
    public String approveAccount(@Valid @ModelAttribute AccountApprovalRequest request,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        // ADD THIS DEBUG LOGGING:

        if (bindingResult.hasErrors()) {
            log.info("=== VALIDATION ERRORS ===");
            bindingResult.getFieldErrors().forEach(error -> {
                log.info("Field: {}, Error: {}, Rejected value: {}",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue());
            });
            log.info("=== END VALIDATION ERRORS ===");

            redirectAttributes.addFlashAttribute("error", "Please correct the validation errors");
        }

        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            Optional<Agent> agent = agentService.getAgentByUserId(user.get().getUserId());
            if (agent.isPresent()) {
                accountService.approveAccountAndCreateUser(
                        request.getAccountNumber(),
                        request.getUsername(),
                        request.getPassword(),
                        agent.get().getAgentId()
                );
                redirectAttributes.addFlashAttribute("message", "Account approved successfully!");
            }
        }
        return "redirect:/agent/dashboard";
    }
    @PostMapping("/deposit")
    public String deposit(@Valid @ModelAttribute TransactionRequest request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the validation errors");
            return "redirect:/agent/dashboard";
        }

        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            Optional<Agent> agent = agentService.getAgentByUserId(user.get().getUserId());
            if (agent.isPresent()) {
                accountService.agentDeposit(request.getAccountNumber(), request.getAmount(), agent.get().getAgentId());
                redirectAttributes.addFlashAttribute("message", "Deposit processed successfully!");
            }
        }
        return "redirect:/agent/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@Valid @ModelAttribute TransactionRequest request,
                           BindingResult bindingResult,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the validation errors");
            return "redirect:/agent/dashboard";
        }

        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            Optional<Agent> agent = agentService.getAgentByUserId(user.get().getUserId());
            if (agent.isPresent()) {
                accountService.agentWithdraw(request.getAccountNumber(), request.getAmount(), agent.get().getAgentId());
                redirectAttributes.addFlashAttribute("message", "Withdrawal processed successfully!");
            }
        }
        return "redirect:/agent/dashboard";
    }

    @PostMapping("/modifications/{accountNumber}/approve")
    public String approveModification(@PathVariable String accountNumber, RedirectAttributes redirectAttributes) {
        accountService.approveAccountModification(accountNumber);
        redirectAttributes.addFlashAttribute("message", "Modification approved successfully!");
        return "redirect:/agent/dashboard";
    }

    @PostMapping("/modifications/{accountNumber}/reject")
    public String rejectModification(@PathVariable String accountNumber, RedirectAttributes redirectAttributes) {
        accountService.rejectAccountModification(accountNumber);
        redirectAttributes.addFlashAttribute("message", "Modification rejected successfully!");
        return "redirect:/agent/dashboard";
    }
}
