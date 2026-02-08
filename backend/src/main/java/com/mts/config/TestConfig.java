package com.mts.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Test environment configuration.
 * Active when spring.profiles.active=test
 * Uses H2 in-memory database for fast, isolated testing.
 */
@Configuration
@Profile("test")
@Slf4j
public class TestConfig {

    @PostConstruct
    public void init() {
        log.info("═══════════════════════════════════════════════════");
        log.info("🧪 TEST ENVIRONMENT ACTIVE");
        log.info("═══════════════════════════════════════════════════");
        log.info("Features enabled:");
        log.info("  ✓ H2 in-memory database");
        log.info("  ✓ Isolated test execution");
        log.info("  ✓ Schema auto-creation (ddl-auto: create-drop)");
        log.info("═══════════════════════════════════════════════════");
    }
}
