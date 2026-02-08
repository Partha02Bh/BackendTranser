package com.mts.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Production environment configuration.
 * Active when spring.profiles.active=prod
 * Optimized for performance and security.
 */
@Configuration
@Profile("prod")
@Slf4j
public class ProdConfig {

    @PostConstruct
    public void init() {
        log.info("═══════════════════════════════════════════════════");
        log.info("🏭 PRODUCTION ENVIRONMENT ACTIVE");
        log.info("═══════════════════════════════════════════════════");
        log.info("Features enabled:");
        log.info("  ✓ Optimized database connection pool");
        log.info("  ✓ Minimal logging (WARN level)");
        log.info("  ✓ Schema validation only (ddl-auto: validate)");
        log.info("  ✓ File-based logging");
        log.info("═══════════════════════════════════════════════════");
    }
}
