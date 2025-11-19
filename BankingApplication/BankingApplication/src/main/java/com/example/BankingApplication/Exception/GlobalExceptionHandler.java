package com.example.BankingApplication.Exception;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, RedirectAttributes redirectAttributes) {
        BindingResult result = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : result.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        redirectAttributes.addFlashAttribute("validationErrors", errors);
        redirectAttributes.addFlashAttribute("error", "Please correct the errors below");

        return "redirect:" + getRedirectUrl(ex);
    }

    @ExceptionHandler(UnderAgeException.class)
    public String handleUnderAgeException(UnderAgeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/public/create-account";
    }

    @ExceptionHandler(InsufficientCustomerBalance.class)
    public String InsufficientCustomerBalance(InsufficientCustomerBalance ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/customer/dashboard";
    }

    @ExceptionHandler({BankingException.class, PasswordMismatchException.class})
    public String handleCustomerProfileExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/customer/profile";
    }

    @ExceptionHandler(CustomerAccountNotFoundException.class)
    public String handleCustomerAccountNotFoundException(CustomerAccountNotFoundException ex,
                                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/customer/dashboard";
    }
    @ExceptionHandler(CustomeralreadyExist.class)
    public String CustomeralreadyExist(CustomeralreadyExist ex,
                                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/public/create-account";
    }


    @ExceptionHandler({AccountNotFoundException.class, InsufficientFundsException.class})
    public String handleAgentExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/agent/dashboard";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard"; // adjust if needed
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("error", "An unexpected error occurred: " + ex.getMessage());
        ex.printStackTrace(); // log for debugging
        return "error"; // generic error page
    }

    private String getRedirectUrl(MethodArgumentNotValidException ex) {
        String methodName = ex.getParameter().getMethod().getName().toLowerCase();
        if (methodName.contains("create")) return "/public/create-account";
        if (methodName.contains("agent")) return "/agent/dashboard";
        if (methodName.contains("admin")) return "/admin/dashboard";
        if (methodName.contains("customer")) return "/customer/dashboard";
        return "/dashboard";
    }
}
