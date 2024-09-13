package com.remindifyapp.config;

import com.remindifyapp.service.AuthUserDetailsService;
import com.remindifyapp.service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthUserDetailsService authUserDetailsService;
    private final JWTService jwtService;

    public SecurityConfig(AuthUserDetailsService authUserDetailsService, JWTService jwtService) {
        this.authUserDetailsService = authUserDetailsService;
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Consider enabling CSRF protection in production
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login", "/users/logout", "/reminders/create", "/reminders/{id}", "/reminders/user/{username}", "/groups/create", "/groups/removeUser/{groupId}/{userId}", "/groups/addUser/{groupId}/{userId}", "/groups/{groupId}/messages", "/groups/{groupId}/reminders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/allUsers","/reminders/user/{username}", "/reminders/{id}", "/reminders/all", "/reminders/by-username", "/groups/all", "/groups/my-groups", "/groups/allUsers/{groupId}", "/bot/chat", "/groups/{groupId}/messages", "/groups/{groupId}/reminders").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/reminders/update/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/reminders/{id}").permitAll()
                        .anyRequest().authenticated()) // All other endpoints require authentication
                .userDetailsService(authUserDetailsService) // Custom UserDetailsService
                .logout(logout -> logout
                        .logoutUrl("/users/logout") // Define the logout URL
                        .logoutSuccessUrl("/users/login") // Redirect URL after successful logout
                        .invalidateHttpSession(true) // Invalidate the session
                        .deleteCookies("JSESSIONID") // Optionally delete cookies
                        .addLogoutHandler((request, response, authentication) -> {
                            // Blacklist the JWT token if applicable
                            String token = extractTokenFromRequest(request);
                            if (token != null) {
                                jwtService.blacklistToken(token);
                            }
                        })).build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(authUserDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Helper method to extract JWT token from request
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
