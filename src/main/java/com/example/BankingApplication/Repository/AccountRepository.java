package com.example.BankingApplication.Repository;

import com.example.BankingApplication.Entity.*;
import com.example.BankingApplication.Entity.Enumeration.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByStatus(AccountStatus status);
    List<Account> findByUserUserId(Long userId);
    Optional<Account> findByNationalId(String nationalId);
    List<Account> findByModificationStatus(ModificationStatus status);
    boolean existsByNationalId(String nationalId);
}
