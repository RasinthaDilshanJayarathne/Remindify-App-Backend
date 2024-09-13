/**
 * Author : rasintha_j
 * Date : 9/9/2024
 * Time : 8:27 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("message")
public class Message {
    @Id
    private String id;  // Use String for MongoDB ObjectId
    private String content;
    private String senderId;  // Reference to the AuthUser who sent the message
    private LocalDateTime sentDate;
}
