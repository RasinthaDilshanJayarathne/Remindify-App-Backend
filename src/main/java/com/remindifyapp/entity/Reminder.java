/**
 * Author : rasintha_j
 * Date : 9/7/2024
 * Time : 7:28 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("reminder")
public class Reminder {
    @Id
    private String id; // Use String for MongoDB IDs

    private String message;
    private String username;
    private String category;
}
