package com.remindifyapp.controller;

import com.remindifyapp.bean.ReminderDTO;
import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.entity.Reminder;
import com.remindifyapp.repository.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class ManageReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ManageReminderController.class);

    private final ReminderRepository reminderRepository;

    @Autowired
    public ManageReminderController(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    // CREATE
    @PostMapping("/addReminder")
    public ResponseDTO<Void> addReminder(@RequestBody ReminderDTO reminderDTO) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Adding reminder for user: {}", reminderDTO.getUsername());

            Reminder reminder = Reminder.builder()
                    .username(reminderDTO.getUsername())
                    .message(reminderDTO.getMessage())
                    .category(reminderDTO.getCategory())
                    .build();

            // Save reminder to the database
            reminderRepository.save(reminder);

            responseDTO.setStatusCode(200);
            responseDTO.setMessage("Reminder added successfully");
            logger.info("Reminder added successfully for user: {}", reminderDTO.getUsername());
        } catch (Exception e) {
            responseDTO.setStatusCode(400);
            responseDTO.setMessage("Failed to add reminder: " + e.getMessage());
            logger.error("Failed to add reminder for user: {}. Error: {}", reminderDTO.getUsername(), e.getMessage());
        }

        return responseDTO;
    }

    // READ by username
    @GetMapping("/getReminderByUsername")
    public ResponseDTO<List<Reminder>> getReminderByUsername(@RequestParam String username) {
        logger.info("Fetching reminders for username: {}", username);
        ResponseDTO<List<Reminder>> responseDTO = new ResponseDTO<>();

        List<Reminder> reminders = reminderRepository.findByUsername(username);
        if (!reminders.isEmpty()) {
            responseDTO.setStatusCode(200);
            responseDTO.setMessage("Successfully retrieved reminders");
            responseDTO.setData(reminders);
            logger.info("Reminders successfully retrieved for username: {}", username);
        } else {
            responseDTO.setStatusCode(404);
            responseDTO.setMessage("User not found");
            responseDTO.setData(null);
            logger.warn("No reminders found for username: {}", username);
        }
        return responseDTO;
    }

    // UPDATE
    @PutMapping("/updateReminder/{id}")
    public ResponseDTO<Void> updateReminder(@PathVariable int id, @RequestBody ReminderDTO reminderDTO) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Updating reminder with ID: {}", id);
            Optional<Reminder> optionalReminder = reminderRepository.findById(id);

            if (optionalReminder.isPresent()) {
                Reminder reminder = optionalReminder.get();
                reminder.setMessage(reminderDTO.getMessage());
                reminder.setCategory(reminderDTO.getCategory());

                // Save updated reminder to the database
                reminderRepository.save(reminder);

                responseDTO.setStatusCode(200);
                responseDTO.setMessage("Reminder updated successfully");
                logger.info("Reminder updated successfully for ID: {}", id);
            } else {
                responseDTO.setStatusCode(404);
                responseDTO.setMessage("Reminder not found");
                logger.warn("Reminder with ID: {} not found", id);
            }
        } catch (Exception e) {
            responseDTO.setStatusCode(400);
            responseDTO.setMessage("Failed to update reminder: " + e.getMessage());
            logger.error("Failed to update reminder with ID: {}. Error: {}", id, e.getMessage());
        }

        return responseDTO;
    }

    // DELETE
    @DeleteMapping("/deleteReminder/{id}")
    public ResponseDTO<Void> deleteReminder(@PathVariable int id) {
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        try {
            logger.info("Deleting reminder with ID: {}", id);
            if (reminderRepository.existsById(id)) {
                reminderRepository.deleteById(id);
                responseDTO.setStatusCode(200);
                responseDTO.setMessage("Reminder deleted successfully");
                logger.info("Reminder deleted successfully for ID: {}", id);
            } else {
                responseDTO.setStatusCode(404);
                responseDTO.setMessage("Reminder not found");
                logger.warn("Reminder with ID: {} not found", id);
            }
        } catch (Exception e) {
            responseDTO.setStatusCode(400);
            responseDTO.setMessage("Failed to delete reminder: " + e.getMessage());
            logger.error("Failed to delete reminder with ID: {}. Error: {}", id, e.getMessage());
        }

        return responseDTO;
    }
}
