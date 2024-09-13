/**
 * Author : rasintha_j
 * Date : 9/9/2024
 * Time : 2:28 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.service;

import com.remindifyapp.entity.Mood;
import com.remindifyapp.repository.MoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodService {
    @Autowired
    private MoodRepository moodRepository;

    public Mood logMood(Mood mood) {
        return moodRepository.save(mood);
    }

    public List<Mood> getUserMoods(String username) {
        return moodRepository.findByUsername(username);
    }
}
