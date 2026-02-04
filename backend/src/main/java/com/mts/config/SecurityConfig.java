package com.mts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/*
 * Spring Security setup - Basic Auth protecting all /api/** endpoints.
 * Using in-memory users for demo purposes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable()) // disabled for REST API
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/**").authenticated()
                                                .anyRequest().permitAll())
                                .httpBasic(Customizer.withDefaults());

                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder encoder) {
                // admin user
                UserDetails admin = User.builder()
                                .username("admin")
                                .password(encoder.encode("admin123"))
                                .roles("ADMIN")
                                .build();

                // regular users - just create them in a list for easier management
                String[] regularUsers = { "user", "alice", "bob", "charlie", "diana", "emma" };

                List<UserDetails> users = Stream.of(regularUsers)
                                .map(name -> User.builder()
                                                .username(name)
                                                .password(encoder.encode(name + "123")) // password = username + "123"
                                                .roles("USER")
                                                .build())
                                .toList();

                // combine admin + regular users
                InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
                manager.createUser(admin);
                users.forEach(manager::createUser);

                return manager;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:4300"));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}
