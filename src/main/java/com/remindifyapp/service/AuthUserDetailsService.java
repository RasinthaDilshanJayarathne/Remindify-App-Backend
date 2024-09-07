package com.remindifyapp.service;

import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.repository.AuthUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public AuthUserDetailsService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(authUser.getUsername(), authUser.getPassword(),
                authUser.isActive(), true, true, true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public List<AuthUser> getAllUsers() {
        return authUserRepository.findAll();
    }

    public List<AuthUser> getAllUsersExcludingUsername(String username) {
        return authUserRepository.findByUsernameNot(username);
    }
}
