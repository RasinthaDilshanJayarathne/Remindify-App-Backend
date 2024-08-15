/**
 * Author : rasintha_j
 * Date : 6/28/2024
 * Time : 3:14 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.repository;

import com.remindifyapp.entity.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, Integer> {
    List<Reminder> findByUsername(String username);
}