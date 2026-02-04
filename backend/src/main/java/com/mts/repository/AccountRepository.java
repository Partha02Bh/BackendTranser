package com.mts.repository;

import com.mts.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Account entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by username.
     * 
     * @param username Account username
     * @return Optional containing account if found
     */
    Optional<Account> findByUsername(String username);
}
