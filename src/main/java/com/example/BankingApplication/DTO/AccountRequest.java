package com.example.BankingApplication.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AccountRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Middle name is required")
    @Size(min = 2, max = 50, message = "Middle name must be between 2 and 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "National ID is required")
    @Size(min = 16, max = 16, message = "National ID must be Your FCN number")
    private String nationalId;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    private String subCity;
    private String woreda;

    @NotBlank(message = "Mother's name is required")
    @Size(min = 2, max = 100, message = "Mother's name must be between 2 and 200 characters")
    private String mothersName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be written as 0906889445")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String email;
}
