package com.mts.config;

import com.mts.domain.enums.Role;
import com.mts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        accountRepository.findByUsername("admin").ifPresent(admin -> {
            if (admin.getRole() != Role.ADMIN) {
                log.info("Updating 'admin' user role to ADMIN");
                admin.setRole(Role.ADMIN);
                accountRepository.save(admin);
                log.info("'admin' user role updated successfully.");
            } else {
                log.info("'admin' user already has ADMIN role.");
            }
        });
    }
}
