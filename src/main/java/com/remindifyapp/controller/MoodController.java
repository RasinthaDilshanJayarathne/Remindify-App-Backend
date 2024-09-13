/**
 * Author : rasintha_j
 * Date : 9/9/2024
 * Time : 2:29 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.controller;

import com.remindifyapp.entity.Mood;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.service.MoodService;
import com.remindifyapp.service.JWTService;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.bean.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/moods")
public class MoodController {

    @Autowired
    private MoodService moodService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @PostMapping("/log")
    public ResponseEntity<ResponseDTO<Mood>> logMood(@Valid @RequestBody Mood mood, BindingResult result, @RequestHeader("Authorization") String token) {

        ResponseDTO<Mood> responseDTO = new ResponseDTO<>();
        String username;

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(", "));
            responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
            responseDTO.setMessage("Validation failed: " + errorMessage);
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            mood.setUsername(username);  // Ensure the mood is associated with the correct user
            Mood loggedMood = moodService.logMood(mood);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Mood logged successfully");
            responseDTO.setData(loggedMood);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while logging the mood");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user-moods")
    public ResponseEntity<ResponseDTO<List<Mood>>> getUserMoods(@RequestHeader("Authorization") String token) {

        ResponseDTO<List<Mood>> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            List<Mood> moods = moodService.getUserMoods(username);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("User moods retrieved successfully");
            responseDTO.setData(moods);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while retrieving the user moods");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
