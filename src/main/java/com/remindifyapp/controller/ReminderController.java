/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 8:04 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.controller;

import com.remindifyapp.entity.Reminder;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.service.JWTService;
import com.remindifyapp.service.ReminderService;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.bean.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/reminders")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<Reminder>> addReminder(@Valid @RequestBody Reminder reminder, BindingResult result, @RequestHeader("Authorization") String token) {

        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        String username;

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(", "));
            responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
            responseDTO.setMessage("Validation failed: " + errorMessage);
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            reminder.setUsername(username);  // Ensure the reminder is associated with the correct user
            Reminder savedReminder = reminderService.addReminder(reminder);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Reminder created successfully");
            responseDTO.setData(savedReminder);
            logger.info("Reminder created successfully: {}", savedReminder);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while creating the reminder");
            logger.error("An error occurred while creating the reminder", e);
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<Reminder>> updateReminder(@PathVariable String id, @Valid @RequestBody Reminder reminder, BindingResult result, @RequestHeader("Authorization") String token) {

        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        String username;

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(", "));
            responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
            responseDTO.setMessage("Validation failed: " + errorMessage);
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            if (!id.equals(reminder.getId())) {
                responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
                responseDTO.setMessage("Reminder ID in the path does not match the ID in the request body");
                logger.warn("Reminder ID mismatch: path ID = {}, body ID = {}", id, reminder.getId());
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            }

            reminder.setUsername(username);  // Ensure the reminder is associated with the correct user
            Reminder updatedReminder = reminderService.updateReminder(reminder);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminder updated successfully");
            responseDTO.setData(updatedReminder);
            logger.info("Reminder updated successfully: {}", updatedReminder);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while updating the reminder");
            logger.error("An error occurred while updating the reminder", e);
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteReminder(@PathVariable String id, @RequestHeader("Authorization") String token) {

        ResponseDTO<Void> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            reminderService.deleteReminder(id);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminder deleted successfully");
            logger.info("Reminder deleted successfully: ID = {}", id);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while deleting the reminder");
            logger.error("An error occurred while deleting the reminder", e);
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<Reminder>>> getAllReminders(@RequestHeader("Authorization") String token) {

        ResponseDTO<List<Reminder>> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            List<Reminder> reminders = reminderService.getAllReminders(username); // Fetch reminders for the user
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminders retrieved successfully");
            responseDTO.setData(reminders);
            logger.info("Retrieved {} reminders for username: {}", reminders.size(), username);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while retrieving the reminders");
            logger.error("An error occurred while retrieving the reminders", e);
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Reminder>> getReminderById(@PathVariable String id, @RequestHeader("Authorization") String token) {

        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            Optional<Reminder> reminder = reminderService.getReminderById(id);
            if (reminder.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.OK.value());
                responseDTO.setMessage("Reminder retrieved successfully");
                responseDTO.setData(reminder.get());
                logger.info("Retrieved reminder successfully: {}", reminder.get());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("Reminder not found");
                logger.warn("Reminder not found: ID = {}", id);
                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while retrieving the reminder");
            logger.error("An error occurred while retrieving the reminder", e);
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
