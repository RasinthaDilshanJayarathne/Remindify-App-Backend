/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 10:44 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String role;
    private String content;//prompt
}
