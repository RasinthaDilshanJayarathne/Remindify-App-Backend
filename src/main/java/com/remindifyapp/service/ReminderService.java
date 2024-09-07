/**
 * Author : rasintha_j
 * Date : 9/7/2024
 * Time : 7:31 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.service;
import com.remindifyapp.entity.Reminder;
import com.remindifyapp.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {
    @Autowired
    private ReminderRepository reminderRepository;

    // Add a new reminder
    public Reminder addReminder(Reminder reminder) {
        return reminderRepository.save(reminder);
    }

    // Update an existing reminder
    public Reminder updateReminder(Reminder reminder) {
        if (reminderRepository.existsById(reminder.getId())) {
            return reminderRepository.save(reminder);
        } else {
            throw new RuntimeException("Reminder not found");
        }
    }

    // Delete a reminder by ID
    public void deleteReminder(String id) {
        reminderRepository.deleteById(id);
    }

    // Get a reminder by ID
    public Optional<Reminder> getReminderById(String id) {
        return reminderRepository.findById(id);
    }

    // Get all reminders for a specific user
    public List<Reminder> getRemindersByUsername(String username) {
        return reminderRepository.findByUsername(username);
    }
}
