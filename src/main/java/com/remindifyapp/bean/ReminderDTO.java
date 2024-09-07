/**
 * Author : rasintha_j
 * Date : 6/28/2024
 * Time : 3:13 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.bean;

import lombok.Data;

@Data
public class ReminderDTO {
    private String message;
    private String username;
    private String category;
}
