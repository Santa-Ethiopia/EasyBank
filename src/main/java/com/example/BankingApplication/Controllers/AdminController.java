package com.example.BankingApplication.Controllers;

import com.example.BankingApplication.DTO.AgentCreationRequest;
import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AgentService agentService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("agents", agentService.getAllAgents());
        model.addAttribute("customers", userService.getAllCustomers());
        return "admin/dashboard";
    }

    @GetMapping("/agents")
    public String agentManagement(Model model) {
        model.addAttribute("agents", agentService.getAllAgents());
        if (!model.containsAttribute("agentRequest")) {
            model.addAttribute("agentRequest", new AgentCreationRequest());
        }
        return "admin/agents";
    }

    @PostMapping("/agents")
    public String createAgent(@Valid @ModelAttribute AgentCreationRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.agentRequest", bindingResult);
            redirectAttributes.addFlashAttribute("agentRequest", request);
            return "redirect:/admin/agents";
        }

        Agent agent = new Agent();
        agent.setBranchName(request.getBranchName());
        agent.setBusinessName(request.getBusinessName());
        agent.setTin(request.getTin());

        agentService.createAgent(agent, request.getUsername(), request.getPassword());
        redirectAttributes.addFlashAttribute("message", "Agent created successfully!");

        return "redirect:/admin/agents";
    }

    @PostMapping("/agents/{agentId}/deposit")
    public String depositToAgent(@PathVariable Long agentId,
                                 @RequestParam BigDecimal amount,
                                 RedirectAttributes redirectAttributes) {
        agentService.depositToAgentBalance(agentId, amount);
        redirectAttributes.addFlashAttribute("message", "Deposit to agent successful!");
        return "redirect:/admin/agents";
    }

    @PostMapping("/agents/{agentId}/deactivate")
    public String deactivateAgent(@PathVariable Long agentId, RedirectAttributes redirectAttributes) {
        agentService.deactivateAgent(agentId);
        redirectAttributes.addFlashAttribute("message", "Agent deactivated successfully!");
        return "redirect:/admin/agents";
    }

    @PostMapping("/agents/{agentId}/activate")
    public String activateAgent(@PathVariable Long agentId, RedirectAttributes redirectAttributes) {
        agentService.activateAgent(agentId);
        redirectAttributes.addFlashAttribute("message", "Agent activated successfully!");
        return "redirect:/admin/agents";
    }

    @PostMapping("/customers/{userId}/deactivate")
    public String deactivateCustomer(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.deactivateUser(userId);
        redirectAttributes.addFlashAttribute("message", "Customer deactivated successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/customers/{userId}/activate")
    public String activateCustomer(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.activateUser(userId);
        redirectAttributes.addFlashAttribute("message", "Customer activated successfully!");
        return "redirect:/admin/dashboard";
    }
}