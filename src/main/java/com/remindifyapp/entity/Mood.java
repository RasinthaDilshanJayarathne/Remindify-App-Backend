/**
 * Author : rasintha_j
 * Date : 9/9/2024
 * Time : 2:26 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("reminder")
public class Mood {
    @Id
    private String id;
    private String username;
    private LocalDate date;
    private String moodLevel;
    private String comments;
}
