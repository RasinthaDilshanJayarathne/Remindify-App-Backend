package com.remindifyapp.config;

import com.remindifyapp.service.AuthUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService authUserDetailsService;


    public SecurityConfig(UserDetailsService authUserDetailsService) {
        this.authUserDetailsService = authUserDetailsService;
    }

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Consider enabling CSRF protection in production
                .cors(Customizer.withDefaults()) // Enable CORS with default configuration
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/getReminderByUsername","/users","/groups", "/error").permitAll() // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/login").permitAll() // Public login endpoint
                        .requestMatchers(HttpMethod.POST, "/addReminder").permitAll()
                        .requestMatchers(HttpMethod.POST, "/create").permitAll()
                        .anyRequest().authenticated()) // All other endpoints require authentication
                .userDetailsService(authUserDetailsService) // Custom UserDetailsService
                .httpBasic(Customizer.withDefaults()) // HTTP Basic authentication
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt password encoder
    }
}
