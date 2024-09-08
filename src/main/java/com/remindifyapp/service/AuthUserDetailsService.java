package com.remindifyapp.service;

import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieve user from the database
        Optional<AuthUser> optionalAuthUser = authUserRepository.findByUsername(username);
        AuthUser authUser = optionalAuthUser.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Return UserDetails implementation
        return authUser;
    }

    public List<AuthUser> findUsersByIds(List<String> userIds) {
        return authUserRepository.findAllById(userIds).stream().filter(user -> userIds.contains(user.getId())).collect(Collectors.toList());
    }
}
