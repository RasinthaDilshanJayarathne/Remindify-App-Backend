package com.remindifyapp.controller;

import com.remindifyapp.bean.LoginDTO;
import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.bean.UserDTO;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.entity.Reminder;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.service.JWTService;
import com.remindifyapp.service.AuthUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/users")
public class AuthUserController {

    private static final Logger logger = LoggerFactory.getLogger(AuthUserController.class);

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;  // JWT utility class
    private final AuthenticationManager authenticationManager;

    public AuthUserController(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder, JWTService jwtService, AuthenticationManager authenticationManager) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    private Optional<AuthUser> authenticateUser(String token) {
        String username = jwtService.extractUsername(token);
        return authUserRepository.findByUsername(username)
                .filter(user -> jwtService.isTokenValid(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<AuthUser>> registerUser(@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date birthday, @RequestParam(value = "image", required = false) MultipartFile image) {

        ResponseDTO<AuthUser> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to register user: {}", username);

            if (authUserRepository.findByUsername(username).isPresent()) {
                logger.warn("Username already exists: {}", username);
                responseDTO.setStatusCode(400);
                responseDTO.setMessage("Username already exists");
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            }

            String imageFilename = null;
            if (image != null && !image.isEmpty()) {
                try {
                    byte[] imageBytes = image.getBytes();
                    // Save imageBytes to database or file system
                    imageFilename = image.getOriginalFilename(); // Or save it as a unique filename
                } catch (IOException e) {
                    logger.error("Error processing image for user: {}", username, e);
                    responseDTO.setStatusCode(400);
                    responseDTO.setMessage("Error processing image");
                    return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
                }
            }

            AuthUser newUser = AuthUser.builder().username(username).email(email).password(passwordEncoder.encode(password)).birthday(birthday).image(imageFilename).active(true).build();
            authUserRepository.save(newUser);

            logger.info("User registered successfully: {}", username);
            responseDTO.setStatusCode(201);
            responseDTO.setMessage("User registered successfully");
            responseDTO.setData(newUser);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error registering user: {}", username, e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while registering the user");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        ResponseDTO<Map<String, Object>> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Attempting to login user: {}", loginDTO.getUsername());

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

            if (authentication.isAuthenticated()) {
                // Obtain UserDetails from Authentication object
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // Fetch AuthUser from the database using the username from UserDetails
                Optional<AuthUser> optionalAuthUser = authUserRepository.findByUsername(userDetails.getUsername());
                if (!optionalAuthUser.isPresent()) {
                    logger.warn("User not found in database: {}", userDetails.getUsername());
                    responseDTO.setStatusCode(404);
                    responseDTO.setMessage("User not found");
                    return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
                }

                AuthUser authUser = optionalAuthUser.get();
                String token = jwtService.generateToken(authUser);  // Generate JWT token

                logger.info("User logged in successfully: {}", loginDTO.getUsername());
                responseDTO.setStatusCode(200);
                responseDTO.setMessage("Login successful");
                responseDTO.setData(Map.of("user", authUser, "token", token));
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                logger.warn("Invalid credentials for user: {}", loginDTO.getUsername());
                responseDTO.setStatusCode(401);
                responseDTO.setMessage("Invalid credentials");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

        } catch (AuthenticationException e) {
            logger.error("Error during login for user: {}", loginDTO.getUsername(), e);
            responseDTO.setStatusCode(500);
            responseDTO.setMessage("An error occurred while logging in");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allUsers")
    public ResponseEntity<ResponseDTO<List<AuthUser>>> getAllUsers(@RequestHeader("Authorization") String token) {
        ResponseDTO<List<AuthUser>> responseDTO = new ResponseDTO<>();

        try {
            // Remove the "Bearer " prefix from the token
            String jwtToken = token.replace("Bearer ", "");

            // Authenticate the user using the token
            Optional<AuthUser> userOptional = authenticateUser(jwtToken);

            // If user authentication fails, return an unauthorized response
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            // Get the currently logged-in user
            AuthUser loggedInUser = userOptional.get();
            String loggedInUsername = loggedInUser.getUsername();

            // Fetch all users from the repository
            List<AuthUser> users = authUserRepository.findAll();

            // Filter out the currently logged-in user from the list
            List<AuthUser> filteredUsers = users.stream()
                    .filter(user -> !user.getUsername().equals(loggedInUsername))
                    .collect(Collectors.toList());

            // If no other users are found, return a not found response
            if (filteredUsers.isEmpty()) {
                logger.warn("No other users found");
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("No other users found");
                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
            }

            // Users found, return them in the response
            logger.info("Users fetched successfully");
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Users fetched successfully");
            responseDTO.setData(filteredUsers);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            // Handle any unexpected exceptions
            logger.error("Error fetching users", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching users");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        jwtService.blacklistToken(jwtToken);
        return ResponseEntity.ok("Logged out successfully");
    }
}
