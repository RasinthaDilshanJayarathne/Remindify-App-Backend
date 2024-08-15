package com.remindifyapp.controller;

import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.entity.ChatGroup;
import com.remindifyapp.service.ChatGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class ChatGroupController {

    private static final Logger logger = LoggerFactory.getLogger(ChatGroupController.class);
    private final ChatGroupService chatGroupService;

    @Autowired
    public ChatGroupController(ChatGroupService chatGroupService) {
        this.chatGroupService = chatGroupService;
    }

    @PostMapping("/create")
    public ResponseDTO<ChatGroup> createChatGroup(@RequestBody ChatGroup chatGroup) {
        logger.info("Creating a new chat group: {}", chatGroup.getGroupname());
        ChatGroup createdChatGroup = chatGroupService.createChatGroup(chatGroup);

        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();
        responseDTO.setStatusCode(200);
        responseDTO.setMessage("Chat group created successfully");
        responseDTO.setData(createdChatGroup);

        return responseDTO;
    }

    @GetMapping("/groups")
    public ResponseDTO<List<ChatGroup>> getAllChatGroups(@RequestParam(required = false) String username) {
        ResponseDTO<List<ChatGroup>> responseDTO = new ResponseDTO<>();
        List<ChatGroup> chatGroups;

        if (username == null || username.isEmpty()) {
            logger.info("Fetching all chat groups.");
            chatGroups = chatGroupService.getAllChatGroups();
        } else {
            logger.info("Fetching chat groups including username: {}", username);
            chatGroups = chatGroupService.getAllUsersIncludingUsername(username);
        }

        responseDTO.setStatusCode(200);
        responseDTO.setMessage("Chat groups fetched successfully");
        responseDTO.setData(chatGroups);

        return responseDTO;
    }

    @GetMapping("/{id}")
    public ResponseDTO<ChatGroup> getChatGroupById(@PathVariable int id) {
        logger.info("Fetching chat group with ID: {}", id);
        Optional<ChatGroup> chatGroup = chatGroupService.getChatGroupById(id);

        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();

        if (chatGroup.isPresent()) {
            responseDTO.setStatusCode(200);
            responseDTO.setMessage("Chat group fetched successfully");
            responseDTO.setData(chatGroup.get());
        } else {
            logger.warn("Chat group with ID: {} not found", id);
            responseDTO.setStatusCode(404);
            responseDTO.setMessage("Chat group not found");
            responseDTO.setData(null);
        }

        return responseDTO;
    }

    @PutMapping("/{id}")
    public ResponseDTO<ChatGroup> updateChatGroup(@PathVariable int id, @RequestBody ChatGroup updatedChatGroup) {
        logger.info("Updating chat group with ID: {}", id);
        ChatGroup updatedGroup = chatGroupService.updateChatGroup(id, updatedChatGroup);

        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();

        if (updatedGroup != null) {
            responseDTO.setStatusCode(200);
            responseDTO.setMessage("Chat group updated successfully");
            responseDTO.setData(updatedGroup);
        } else {
            logger.warn("Chat group with ID: {} not found for update", id);
            responseDTO.setStatusCode(404);
            responseDTO.setMessage("Chat group not found for update");
            responseDTO.setData(null);
        }

        return responseDTO;
    }

    @DeleteMapping("/{id}")
    public ResponseDTO<Void> deleteChatGroup(@PathVariable int id) {
        logger.info("Deleting chat group with ID: {}", id);
        chatGroupService.deleteChatGroup(id);

        ResponseDTO<Void> responseDTO = new ResponseDTO<>();
        responseDTO.setStatusCode(204);
        responseDTO.setMessage("Chat group deleted successfully");
        responseDTO.setData(null);

        return responseDTO;
    }
}
