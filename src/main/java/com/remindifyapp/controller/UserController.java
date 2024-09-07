package com.remindifyapp.controller;

import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.bean.UserDTO;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE: Register User
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<Void>> registerUser(@Valid @RequestBody UserDTO userDTO) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to register user: {}", userDTO.getUsername());

            if (authUserRepository.findByUsername(userDTO.getUsername()).isPresent()) {
                logger.warn("Username already exists: {}", userDTO.getUsername());
                responseDTO.setStatusCode(400);
                responseDTO.setMessage("Username already exists");
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            }

            if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
                try {
                    byte[] imageBytes = ImageUtils.decodeBase64ToImage(userDTO.getImage());
                    // Save imageBytes to database or file system
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid Base64 string for user: {}", userDTO.getUsername(), e);
                    responseDTO.setStatusCode(400);
                    responseDTO.setMessage("Invalid Base64 string");
                    return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
                }
            }

            AuthUser newUser = AuthUser.builder()
                    .username(userDTO.getUsername())
                    .email(userDTO.getEmail())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .birthday(userDTO.getBirthday())
                    .image(userDTO.getImage())
                    .active(true)
                    .build();
            authUserRepository.save(newUser);

            logger.info("User registered successfully: {}", userDTO.getUsername());
            responseDTO.setStatusCode(201);
            responseDTO.setMessage("User registered successfully");
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error registering user: {}", userDTO.getUsername(), e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while registering the user");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ: Login User
    @PostMapping("/login")
    public ResponseDTO<AuthUser> loginUser(@RequestParam String username, @RequestParam String password) {
        ResponseDTO<AuthUser> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to login user: {}", username);

            Optional<AuthUser> optionalUser = authUserRepository.findByUsername(username);
            if (optionalUser.isPresent()) {
                AuthUser authUser = optionalUser.get();
                if (passwordEncoder.matches(password, authUser.getPassword())) {
                    logger.info("User logged in successfully: {}", username);
                    responseDTO.setStatusCode(200);
                    responseDTO.setMessage("Login successful");
                    responseDTO.setData(authUser);
                } else {
                    logger.warn("Invalid password for user: {}", username);
                    responseDTO.setStatusCode(401);
                    responseDTO.setMessage("Invalid password");
                    responseDTO.setData(null);
                }
            } else {
                logger.warn("User not found: {}", username);
                responseDTO.setStatusCode(404);
                responseDTO.setMessage("User not found");
                responseDTO.setData(null);
            }

        } catch (Exception e) {
            logger.error("Error during login for user: {}", username, e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while logging in");
            responseDTO.setData(null);
        }

        return responseDTO;
    }

    // READ: Get User by Username
    @GetMapping("/{username}")
    public ResponseDTO<AuthUser> getUserByUsername(@PathVariable String username) {
        ResponseDTO<AuthUser> responseDTO = new ResponseDTO<>();

        logger.info("Fetching user by username: {}", username);
        Optional<AuthUser> optionalUser = authUserRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            responseDTO.setStatusCode(200);
            responseDTO.setMessage("User retrieved successfully");
            responseDTO.setData(optionalUser.get());
        } else {
            responseDTO.setStatusCode(404);
            responseDTO.setMessage("User not found");
            responseDTO.setData(null);
            logger.warn("User not found with username: {}", username);
        }

        return responseDTO;
    }

    // UPDATE: Update User
    @PutMapping("/{username}")
    public ResponseDTO<Void> updateUser(@PathVariable String username, @Valid @RequestBody UserDTO userDTO) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Updating user: {}", username);
            Optional<AuthUser> optionalUser = authUserRepository.findByUsername(username);

            if (optionalUser.isPresent()) {
                AuthUser existingUser = optionalUser.get();
                existingUser.setEmail(userDTO.getEmail());
                existingUser.setBirthday(userDTO.getBirthday());

                if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                }

                if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
                    try {
                        byte[] imageBytes = ImageUtils.decodeBase64ToImage(userDTO.getImage());
                        // Save imageBytes to database or file system
                        existingUser.setImage(userDTO.getImage());
                    } catch (IllegalArgumentException e) {
                        logger.error("Invalid Base64 string for user: {}", userDTO.getUsername(), e);
                        responseDTO.setStatusCode(400);
                        responseDTO.setMessage("Invalid Base64 string");
                        return responseDTO;
                    }
                }

                authUserRepository.save(existingUser);
                responseDTO.setStatusCode(200);
                responseDTO.setMessage("User updated successfully");
                logger.info("User updated successfully: {}", username);
            } else {
                responseDTO.setStatusCode(404);
                responseDTO.setMessage("User not found");
                logger.warn("User not found with username: {}", username);
            }

        } catch (Exception e) {
            logger.error("Error updating user: {}", username, e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while updating the user");
        }

        return responseDTO;
    }

    // DELETE: Delete User
    @DeleteMapping("/{username}")
    public ResponseDTO<Void> deleteUser(@PathVariable String username) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Deleting user: {}", username);
            Optional<AuthUser> optionalUser = authUserRepository.findByUsername(username);

            if (optionalUser.isPresent()) {
                authUserRepository.delete(optionalUser.get());
                responseDTO.setStatusCode(200);
                responseDTO.setMessage("User deleted successfully");
                logger.info("User deleted successfully: {}", username);
            } else {
                responseDTO.setStatusCode(404);
                responseDTO.setMessage("User not found");
                logger.warn("User not found with username: {}", username);
            }

        } catch (Exception e) {
            logger.error("Error deleting user: {}", username, e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while deleting the user");
        }

        return responseDTO;
    }
}
