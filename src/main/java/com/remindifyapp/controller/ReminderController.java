/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 8:04 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.controller;

import com.remindifyapp.bean.ReminderDTO;
import com.remindifyapp.entity.Reminder;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.service.JWTService;
import com.remindifyapp.service.ReminderService;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.bean.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
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

    private Optional<AuthUser> authenticateUser(String token) {
        String username = jwtService.extractUsername(token);
        return authUserRepository.findByUsername(username)
                .filter(user -> jwtService.isTokenValid(token, user));
    }

//    @PostMapping("/create")
//    public ResponseEntity<ResponseDTO<Reminder>> addReminder(
//            @Valid @RequestBody Reminder reminder,
//            BindingResult result,
//            @RequestHeader("Authorization") String token) {
//
//        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
//        if (result.hasErrors()) {
//            String errorMessage = result.getAllErrors().stream()
//                    .map(error -> error.getDefaultMessage())
//                    .collect(Collectors.joining(", "));
//            responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
//            responseDTO.setMessage("Validation failed: " + errorMessage);
//            logger.warn("Validation failed: {}", errorMessage);
//            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
//        }
//
//        try {
//            Optional<AuthUser> userOptional = authenticateUser(token);
//            if (!userOptional.isPresent()) {
//                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
//                responseDTO.setMessage("Invalid or expired token");
//                logger.warn("Invalid or expired token");
//                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
//            }
//
//            // Set both username and createdBy to the same value
//            String username = userOptional.get().getUsername();
//            reminder.setUsername(username);
//            reminder.setCreatedBy(username);
//
//            Reminder savedReminder = reminderService.addReminder(reminder);
//            responseDTO.setStatusCode(HttpStatus.CREATED.value());
//            responseDTO.setMessage("Reminder created successfully");
//            responseDTO.setData(savedReminder);
//            logger.info("Reminder created successfully: {}", savedReminder);
//            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
//
//        } catch (Exception e) {
//            logger.error("An error occurred while creating the reminder", e);
//            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            responseDTO.setMessage("An error occurred while creating the reminder");
//            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<Reminder>> addReminder(
            @Valid @RequestBody ReminderDTO reminderDto,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        Reminder reminder = new Reminder();

        try {
            // Authenticate user with the provided token
            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            // Get the authenticated user's username
            String username = userOptional.get().getUsername();

            // Call Flask API to process the paragraph (from reminder description)
            RestTemplate restTemplate = new RestTemplate();
            String flaskApiUrl = "http://localhost:6748/process_paragraph";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prepare the request body to send to Flask API
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("paragraph", reminderDto.getParagraph());  // Pass reminder description as paragraph
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            try {
                // Make the POST request to Flask API
                ResponseEntity<String> flaskResponse = restTemplate.postForEntity(flaskApiUrl, request, String.class);

                if (flaskResponse.getStatusCode() == HttpStatus.OK) {
                    String flaskResult = flaskResponse.getBody();

                    // Parsing Flask API response, assuming a specific format
                    String[] parsedResult = flaskResult.split(", ");
                    if (parsedResult.length >= 5) {
                        String title = parsedResult[0].replace("Title: ", "").trim();;
                        String venue = parsedResult[1].replace("Venue: ", "").trim();;
                        String date = parsedResult[2].replace("Date: ", "").trim();;
                        String time = parsedResult[3].replace("Time: ", "").trim();;
                        String category = parsedResult[4].replace("Category: ", "").trim();;

                        title = title.replace("\"", "").trim();
                        category = category.replace("\"", "").trim();

                        // Set the parsed values to the Reminder object
                        reminder.setTitle(title);
                        reminder.setVenue(venue);  // Setting venue, date, and time as description
                        reminder.setCategory(category);
                        reminder.setReminderDate(date);
                        reminder.setReminderTime(time);
                        reminder.setUsername(username);
                        reminder.setCreatedBy(username);
                    } else {
                        logger.error("Unexpected response format from Flask API: {}", flaskResult);
                        throw new RuntimeException("Invalid Flask API response format");
                    }
                } else {
                    logger.error("Flask API returned non-OK status: {}", flaskResponse.getStatusCode());
                    throw new RuntimeException("Failed to process paragraph with Flask API");
                }
            } catch (HttpClientErrorException e) {
                logger.error("Flask API Client Error: {}", e.getMessage());
                throw new RuntimeException("Error in connecting to Flask API");
            } catch (Exception e) {
                logger.error("An unexpected error occurred while processing Flask API response", e);
                throw new RuntimeException("Error processing Flask API response");
            }

            // Save the reminder to the database
            Reminder savedReminder = reminderService.addReminder(reminder);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Reminder created successfully");
            responseDTO.setData(savedReminder);
            logger.info("Reminder created successfully: {}", savedReminder);

            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("An error occurred while creating the reminder", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while creating the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<Reminder>> updateReminder(
            @PathVariable String id,
            @Valid @RequestBody Reminder reminder,
            BindingResult result,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
            responseDTO.setMessage("Validation failed: " + errorMessage);
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            String username = userOptional.get().getUsername();
            Optional<Reminder> existingReminderOptional = reminderService.getReminderById(id);

            if (!existingReminderOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("Reminder not found");
                logger.warn("Reminder not found: ID = {}", id);
                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
            }

            Reminder existingReminder = existingReminderOptional.get();
            if (!existingReminder.getUsername().equals(username)) {
                responseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
                responseDTO.setMessage("You are not authorized to update this reminder");
                logger.warn("Unauthorized update attempt: Reminder ID = {}, Username = {}", id, username);
                return new ResponseEntity<>(responseDTO, HttpStatus.FORBIDDEN);
            }

            if (!id.equals(reminder.getId())) {
                responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
                responseDTO.setMessage("Reminder ID in the path does not match the ID in the request body");
                logger.warn("Reminder ID mismatch: path ID = {}, body ID = {}", id, reminder.getId());
                return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
            }

            // Set both username and createdBy to the same value
            reminder.setUsername(username);
            reminder.setCreatedBy(username);

            Reminder updatedReminder = reminderService.updateReminder(reminder);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminder updated successfully");
            responseDTO.setData(updatedReminder);
            logger.info("Reminder updated successfully: {}", updatedReminder);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("An error occurred while updating the reminder", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while updating the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteReminder(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<Void> responseDTO = new ResponseDTO<>();
        try {
            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            String username = userOptional.get().getUsername();
            Optional<Reminder> existingReminderOptional = reminderService.getReminderById(id);

            if (!existingReminderOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("Reminder not found");
                logger.warn("Reminder not found: ID = {}", id);
                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
            }

            Reminder existingReminder = existingReminderOptional.get();
            if (!existingReminder.getUsername().equals(username)) {
                responseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
                responseDTO.setMessage("You are not authorized to delete this reminder");
                logger.warn("Unauthorized delete attempt: Reminder ID = {}, Username = {}", id, username);
                return new ResponseEntity<>(responseDTO, HttpStatus.FORBIDDEN);
            }

            reminderService.deleteReminder(id);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminder deleted successfully");
            logger.info("Reminder deleted successfully: ID = {}", id);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("An error occurred while deleting the reminder", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while deleting the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<Reminder>>> getAllReminders(@RequestHeader("Authorization") String token) {

        ResponseDTO<List<Reminder>> responseDTO = new ResponseDTO<>();
        try {
            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            List<Reminder> reminders = reminderService.getAllReminders(userOptional.get().getUsername());
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminders retrieved successfully");
            responseDTO.setData(reminders);
            logger.info("Retrieved {} reminders for username: {}", reminders.size(), userOptional.get().getUsername());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("An error occurred while retrieving the reminders", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while retrieving the reminders");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Reminder>> getReminderById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        try {
            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
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
            logger.error("An error occurred while retrieving the reminder", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while retrieving the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-username")
    public ResponseEntity<ResponseDTO<List<Reminder>>> getRemindersByUsername(
            @RequestParam String username,
            @RequestHeader("Authorization") String token) {

        ResponseDTO<List<Reminder>> responseDTO = new ResponseDTO<>();
        try {

            Optional<AuthUser> userOptional = authenticateUser(token);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token");
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            String authenticatedUsername = userOptional.get().getUsername();
            if (!authenticatedUsername.equals(username)) {
                responseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
                responseDTO.setMessage("You are not authorized to view reminders for this user");
                logger.warn("Unauthorized access attempt: Requested username = {}, Authenticated username = {}", username, authenticatedUsername);
                return new ResponseEntity<>(responseDTO, HttpStatus.FORBIDDEN);
            }

            List<Reminder> reminders = reminderService.findRemindersByUsername(username);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminders retrieved successfully");
            responseDTO.setData(reminders);
            logger.info("Retrieved {} reminders for username: {}", reminders.size(), username);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("An error occurred while retrieving reminders by username", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while retrieving reminders by username");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
