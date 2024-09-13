/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 10:43 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGPTRequestDTO {
    private String model;
    private List<MessageDTO> messageDTOS;

    public ChatGPTRequestDTO(String model, String prompt) {
        this.model = model;
        this.messageDTOS = new ArrayList<>();
        this.messageDTOS.add(new MessageDTO("user",prompt));
    }
}
