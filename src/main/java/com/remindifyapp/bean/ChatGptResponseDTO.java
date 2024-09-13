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

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGptResponseDTO {
    private List<Choice> choices;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {

        private int index;
        private MessageDTO messageDTO;

    }
}
