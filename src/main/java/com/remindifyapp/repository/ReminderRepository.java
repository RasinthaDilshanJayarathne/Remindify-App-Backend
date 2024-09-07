/**
 * Author : rasintha_j
 * Date : 9/7/2024
 * Time : 7:30 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.repository;

import com.remindifyapp.entity.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    // Custom query methods can be added here if needed
    List<Reminder> findByUsername(String username);
}
