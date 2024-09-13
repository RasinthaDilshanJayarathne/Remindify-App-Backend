/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 10:41 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.controller;

import com.remindifyapp.bean.ChatGPTRequestDTO;
import com.remindifyapp.bean.ChatGptResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bot")
public class CustomBotController {
    @Value("${openai.model}")
    private String model;

    @Value(("${openai.api.url}"))
    private String apiURL;

    @Autowired
    private RestTemplate template;

    /*@GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        try {
            ChatGPTRequest request = new ChatGPTRequest(model, prompt);
            ChatGptResponse chatGptResponse = template.postForObject(apiURL, request, ChatGptResponse.class);
            return chatGptResponse.getChoices().get(0).getMessage().getContent();
        } catch (HttpClientErrorException.TooManyRequests e) {
            // Handle quota exceeded error
            return "You have exceeded your API quota. Please try again later.";
        }
    }*/
    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        Logger logger = LoggerFactory.getLogger(CustomBotController.class);
        int retries = 0;
        int maxRetries = 3;
        long waitTime = 1000; // initial wait time in milliseconds

        while (retries < maxRetries) {
            try {
                ChatGPTRequestDTO request = new ChatGPTRequestDTO(model, prompt);
                ChatGptResponseDTO chatGptResponseDTO = template.postForObject(apiURL, request, ChatGptResponseDTO.class);
                return chatGptResponseDTO.getChoices().get(0).getMessageDTO().getContent();
            } catch (HttpClientErrorException.TooManyRequests e) {
                retries++;
                logger.warn("Quota exceeded. Retry {} of {}. Waiting {}ms", retries, maxRetries, waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "An error occurred while processing your request.";
                }
                waitTime *= 2; // Exponential backoff
            } catch (Exception e) {
                logger.error("An error occurred: {}", e.getMessage());
                return "An unexpected error occurred. Please try again later.";
            }
        }
        return "You have exceeded your API quota. Please try again later.";
    }

}
