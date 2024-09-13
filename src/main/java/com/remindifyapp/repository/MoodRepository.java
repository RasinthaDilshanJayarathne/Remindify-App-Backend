/**
 * Author : rasintha_j
 * Date : 9/9/2024
 * Time : 2:27 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.repository;

import com.remindifyapp.entity.Mood;
import com.remindifyapp.entity.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MoodRepository extends MongoRepository<Mood, String> {
    List<Mood> findByUsername(String userId);
}
