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
    /*public SecurityFilterChain defaultFilterChain(HttpSecurity httpSecurity) throws Exception {
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
    }*/
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Consider enabling CSRF protection if you're using session-based authentication
                .cors(Customizer.withDefaults()) // Enable CORS with default configuration
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/register", "/users/login", "/reminders/getReminderByUsername", "/users/allUsers", "/groups/AllGroups", "/groups/getGroupById/{id}","/error").permitAll() // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/reminders/addReminder","/groups/create").permitAll() // Public access to create
                        .requestMatchers(HttpMethod.PUT, "/users/{username}","/reminders/updateReminder/{id}","/updateGroup/groups/{id}").authenticated() // Require authentication for PUT requests to update user details
                        .requestMatchers(HttpMethod.DELETE, "/users/{username}","/reminders/deleteReminder/{id}","/groups/deleteGroup/{id}").authenticated() // Require authentication for DELETE requests
                        .anyRequest().authenticated() // All other endpoints require authentication
                )
                .userDetailsService(authUserDetailsService) // Custom UserDetailsService
                .httpBasic(Customizer.withDefaults()) // Enable HTTP Basic authentication
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt password encoder
    }
}
