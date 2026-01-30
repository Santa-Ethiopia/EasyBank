package com.example.BankingApplication.Controllers;

import com.example.BankingApplication.DTO.*;
import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Exception.*;
import com.example.BankingApplication.Service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final AccountService accountService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            model.addAttribute("accounts", accountService.getAccountsByUser(user.get().getUserId()));
        }
        model.addAttribute("transferRequest", new TransferRequest());
        return "customer/dashboard";
    }

    @GetMapping("/transactions")
    public String transactions(@RequestParam String accountNumber, Model model) {
        model.addAttribute("transactions", accountService.getTransactionHistory(accountNumber));
        model.addAttribute("accountNumber", accountNumber);
        return "customer/transactions";
    }

    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute TransferRequest request,
                           BindingResult bindingResult,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the validation errors");
            return "redirect:/customer/dashboard";
        }

        accountService.transferBetweenAccounts(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount(),
                authentication.getName()
        );
        redirectAttributes.addFlashAttribute("message", "Transfer completed successfully!");
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            var accounts = accountService.getAccountsByUser(user.get().getUserId());
            if (!accounts.isEmpty()) {
                model.addAttribute("account", accounts.get(0));
            }
        }
        model.addAttribute("passwordRequest", new PasswordChangeRequest());
        return "customer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Account account,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            var accounts = accountService.getAccountsByUser(user.get().getUserId());
            if (!accounts.isEmpty()) {
                accountService.requestAccountModification(accounts.get(0).getAccountNumber(), account);
                redirectAttributes.addFlashAttribute("message", "Profile update requested! Waiting for agent approval.");
            }
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeRequest request,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct validation errors");
            return "redirect:/customer/profile";
        }

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userService.changePassword(user, request.getCurrentPassword(),
                request.getNewPassword(), request.getConfirmPassword());

        redirectAttributes.addFlashAttribute("message", "Password changed successfully!");
        return "redirect:/customer/profile";
    }
}