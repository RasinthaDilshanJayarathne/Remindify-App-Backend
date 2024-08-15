/**
 * Author : rasintha_j
 * Date : 8/9/2024
 * Time : 2:07 PM
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
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("group")
public class ChatGroup {
    @Id
    private String groupname;
    private List<String> members;  // Assuming user IDs are integers
    private LocalDateTime createdDate;
    private String status;  // e.g., "active", "archived"
}
