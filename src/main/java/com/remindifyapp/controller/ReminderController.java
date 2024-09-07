package com.remindifyapp.controller;

import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.entity.Reminder;
import com.remindifyapp.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/reminders")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    @Autowired
    private ReminderService reminderService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<Reminder>> addReminder(@RequestBody Reminder reminder) {
        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        try {
            logger.info("Attempting to create a reminder: {}", reminder);

            Reminder savedReminder = reminderService.addReminder(reminder);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Reminder created successfully");
            responseDTO.setData(savedReminder);

            logger.info("Reminder created successfully: {}", savedReminder);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating reminder: {}", reminder, e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while creating the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    public ResponseEntity<ResponseDTO<Reminder>> updateReminder(@RequestBody Reminder reminder) {
        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        try {
            logger.info("Attempting to update a reminder: {}", reminder);

            Reminder updatedReminder = reminderService.updateReminder(reminder);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminder updated successfully");
            responseDTO.setData(updatedReminder);

            logger.info("Reminder updated successfully: {}", updatedReminder);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating reminder: {}", reminder, e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while updating the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteReminder(@PathVariable String id) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();
        try {
            logger.info("Attempting to delete reminder with id: {}", id);

            reminderService.deleteReminder(id);
            responseDTO.setStatusCode(HttpStatus.NO_CONTENT.value());
            responseDTO.setMessage("Reminder deleted successfully");

            logger.info("Reminder deleted successfully with id: {}", id);
            return new ResponseEntity<>(responseDTO, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting reminder with id: {}", id, e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while deleting the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Reminder>> getReminderById(@PathVariable String id) {
        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        try {
            logger.info("Attempting to fetch reminder with id: {}", id);

            Optional<Reminder> reminder = reminderService.getReminderById(id);
            if (reminder.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.OK.value());
                responseDTO.setMessage("Reminder fetched successfully");
                responseDTO.setData(reminder.get());
                logger.info("Reminder fetched successfully: {}", reminder.get());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("Reminder not found");
                logger.warn("Reminder not found with id: {}", id);
                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error fetching reminder with id: {}", id, e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching the reminder");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<ResponseDTO<List<Reminder>>> getRemindersByUsername(@PathVariable String username) {
        ResponseDTO<List<Reminder>> responseDTO = new ResponseDTO<>();
        try {
            logger.info("Attempting to fetch reminders for user: {}", username);

            List<Reminder> reminders = reminderService.getRemindersByUsername(username);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Reminders fetched successfully");
            responseDTO.setData(reminders);

            logger.info("Reminders fetched successfully for user: {}", username);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching reminders for user: {}", username, e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching reminders");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
