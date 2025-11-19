package com.example.BankingApplication.Service;

import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import com.example.BankingApplication.Exception.*;
import com.example.BankingApplication.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public User createUser(String username, String password, UserRole role) {
        Role userRole = roleRepository.findByUserRole(role)
                .orElseThrow(() -> new BankingException("Role not found: " + role));

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(userRole);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword, String confirmPassword) {

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BankingException("Current password is incorrect");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("New passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllCustomers() {
        return userRepository.findByRoleUserRole(UserRole.ROLE_CUSTOMER);
    }

    public List<User> getAllAgents() {
        return userRepository.findByRoleUserRole(UserRole.ROLE_AGENT);
    }
}