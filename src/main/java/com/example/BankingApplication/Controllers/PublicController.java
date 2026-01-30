package com.example.BankingApplication.Controllers;

import com.example.BankingApplication.DTO.AccountRequest;
import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import com.example.BankingApplication.Service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final AccountService accountService;

    @GetMapping("/create-account")
    public String createAccountForm(Model model) {
        model.addAttribute("accountRequest", new AccountRequest());
        return "public/create-account";
    }

    @PostMapping("/create-account")
    public String createAccount(@Valid @ModelAttribute AccountRequest request,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            System.out.println("=== VALIDATION ERRORS ===");
            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println("Field: " + error.getField() + ", Error: " + error.getDefaultMessage());
            });

            return "public/create-account";
        }

        Account account = new Account();
        account.setFirstName(request.getFirstName());
        account.setMiddleName(request.getMiddleName());
        account.setLastName(request.getLastName());
        account.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        account.setBirthDate(request.getBirthDate());
        account.setNationalId(request.getNationalId());
        account.setCity(request.getCity());
        account.setSubCity(request.getSubCity());
        account.setWoreda(request.getWoreda());
        account.setMothersName(request.getMothersName());
        account.setMobileNumber(request.getMobileNumber());
        account.setEmail(request.getEmail());

        Account createdAccount = accountService.createPendingAccount(account);
        model.addAttribute("message", "Account created successfully! Please wait for agent approval. Your account number: " + createdAccount.getAccountNumber());
        return "public/account-created";
    }
}
