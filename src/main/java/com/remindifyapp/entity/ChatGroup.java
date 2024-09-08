/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 8:00 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("group")
public class ChatGroup {
    @Id
    private String id;  // Use String for MongoDB ObjectId
    private String groupname;
    @DBRef
    private List<AuthUser> members;  // List of member AuthUser entities
    private LocalDateTime createdDate;
    private String status;  // e.g., "active", "archived"
}
