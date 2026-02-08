package com.mts.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Development environment configuration.
 * Active when spring.profiles.active=dev
 */
@Configuration
@Profile("dev")
@Slf4j
public class DevConfig {

    @PostConstruct
    public void init() {
        log.info("═══════════════════════════════════════════════════");
        log.info("🚀 DEVELOPMENT ENVIRONMENT ACTIVE");
        log.info("═══════════════════════════════════════════════════");
        log.info("Features enabled:");
        log.info("  ✓ Verbose SQL logging");
        log.info("  ✓ Debug-level logging");
        log.info("  ✓ Schema auto-creation (ddl-auto: create)");
        log.info("═══════════════════════════════════════════════════");
    }
}
