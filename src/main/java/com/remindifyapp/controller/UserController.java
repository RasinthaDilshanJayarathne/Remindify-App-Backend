package com.remindifyapp.controller;

import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.bean.UserDTO;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.util.ImageUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<Void>> registerUser(@Valid @RequestBody UserDTO userDTO) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        if (authUserRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            responseDTO.setStatusCode(400);
            responseDTO.setMessage("Username already exists");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            try {
                byte[] imageBytes = ImageUtils.decodeBase64ToImage(userDTO.getImage());
                // Save imageBytes to database or file system
            } catch (IllegalArgumentException e) {
                responseDTO.setStatusCode(400);
                responseDTO.setMessage("Invalid Base64 string");
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                responseDTO.setStatusCode(500);
                responseDTO.setMessage("Failed to save image");
                return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
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

        responseDTO.setStatusCode(201);
        responseDTO.setMessage("User registered successfully");
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseDTO<AuthUser> loginUser(@RequestParam String username, @RequestParam String password) {
        ResponseDTO<AuthUser> responseDTO = new ResponseDTO<>();

        Optional<AuthUser> optionalUser = authUserRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            AuthUser authUser = optionalUser.get();
            if (passwordEncoder.matches(password, authUser.getPassword())) {
                responseDTO.setStatusCode(200);
                responseDTO.setMessage("success");
                responseDTO.setData(authUser);
            } else {
                responseDTO.setStatusCode(401);
                responseDTO.setMessage("Invalid password");
                responseDTO.setData(null);
            }
        } else {
            responseDTO.setStatusCode(404);
            responseDTO.setMessage("User not found");
            responseDTO.setData(null);
        }

        return responseDTO;
    }
}
