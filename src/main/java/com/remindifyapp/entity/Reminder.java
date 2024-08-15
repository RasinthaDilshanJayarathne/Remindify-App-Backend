/**
 * Author : rasintha_j
 * Date : 6/28/2024
 * Time : 3:03 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("reminder")
public class Reminder {
    @Id
    private int id;
    private String message;
    private String username;
}
