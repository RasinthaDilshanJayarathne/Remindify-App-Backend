package com.remindifyapp.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Document("user")
public class AuthUser implements UserDetails {

    @Id
    private String id = UUID.randomUUID().toString();
    private String username;
    private String email;
    private String password;
    private Date birthday;
    private String image;
    private boolean active;

    // Optionally, you can add authorities if needed
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : List.of(); // Return an empty list if authorities are not set
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active; // You can change this based on your requirements
    }

    @Override
    public boolean isAccountNonLocked() {
        return active; // You can change this based on your requirements
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active; // You can change this based on your requirements
    }

    @Override
    public boolean isEnabled() {
        return active; // You can change this based on your requirements
    }
}
