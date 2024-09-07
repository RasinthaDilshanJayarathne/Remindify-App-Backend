package com.remindifyapp.repository;

import com.remindifyapp.entity.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {
    Optional<AuthUser> findByUsername(String username);
    List<AuthUser> findByUsernameNot(String username);
    Optional<AuthUser> findByEmail(String email);

}
