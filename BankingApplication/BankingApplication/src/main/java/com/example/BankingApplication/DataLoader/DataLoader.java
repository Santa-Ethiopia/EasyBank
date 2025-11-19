package com.example.BankingApplication.DataLoader;

import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import com.example.BankingApplication.Repository.*;
import lombok.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(UserRole.ROLE_ADMIN));
            roleRepository.save(new Role(UserRole.ROLE_CUSTOMER));
            roleRepository.save(new Role(UserRole.ROLE_AGENT));
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByUserRole(UserRole.ROLE_ADMIN).get();
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
        }
    }
}
