/**
 * Author : rasintha_j
 * Date : 8/9/2024
 * Time : 2:16 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.bean;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatGroupDTO {
    private String groupname;
    private List<Integer> members;  // Assuming user IDs are integers
    private LocalDateTime createdDate;
    private String status;  // e.g., "active", "archived"
}
