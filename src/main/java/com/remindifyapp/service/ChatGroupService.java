package com.remindifyapp.service;

import com.remindifyapp.controller.ChatGroupController;
import com.remindifyapp.entity.ChatGroup;
import com.remindifyapp.repository.ChatGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ChatGroupController.class);

    @Autowired
    private ChatGroupRepository chatGroupRepository;
    private String username;

    public ChatGroup createChatGroup(ChatGroup chatGroup) {
        chatGroup.setCreatedDate(LocalDateTime.now());
        chatGroup.setStatus("active");

        try {
            return chatGroupRepository.save(chatGroup);
        } catch (Exception e) {
            logger.error("Error saving chat group: {}", e.getMessage());
            throw new RuntimeException("Failed to create chat group", e);
        }
    }

    public List<ChatGroup> getAllChatGroups() {
        try {
            return chatGroupRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving chat groups: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve chat groups", e);
        }
    }

    public Optional<ChatGroup> getChatGroupById(int id) {
        try {
            return chatGroupRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving chat group by ID: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve chat group by ID", e);
        }
    }

    public List<ChatGroup> getChatGroupsByGroupName(String groupname) {
        try {
            return chatGroupRepository.findByGroupname(groupname);
        } catch (Exception e) {
            logger.error("Error retrieving chat groups by group name: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve chat groups by group name", e);
        }
    }

    public ChatGroup updateChatGroup(int id, ChatGroup updatedChatGroup) {
        try {
            return chatGroupRepository.findById(id)
                    .map(chatGroup -> {
                        if (updatedChatGroup.getGroupname() != null) {
                            chatGroup.setGroupname(updatedChatGroup.getGroupname());
                        }
                        if (updatedChatGroup.getMembers() != null) {
                            chatGroup.setMembers(updatedChatGroup.getMembers());
                        }
                        if (updatedChatGroup.getStatus() != null) {
                            chatGroup.setStatus(updatedChatGroup.getStatus());
                        }
                        return chatGroupRepository.save(chatGroup);
                    }).orElseThrow(() -> {
                        logger.error("Chat group not found with ID: {}", id);
                        return new EntityNotFoundException("Chat group not found");
                    });
        } catch (Exception e) {
            logger.error("Error updating chat group: {}", e.getMessage());
            throw new RuntimeException("Failed to update chat group", e);
        }
    }

    public void deleteChatGroup(int id) {
        try {
            if (!chatGroupRepository.existsById(id)) {
                throw new EntityNotFoundException("Chat group not found");
            }
            chatGroupRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error deleting chat group: {}", e.getMessage());
            throw new RuntimeException("Failed to delete chat group", e);
        }
    }

    public List<ChatGroup> getAllUsersIncludingUsername(String username) {
        this.username = username;
        try {
            List<ChatGroup> allGroups = chatGroupRepository.findAll();
            return allGroups.stream()
                    .filter(group -> group.getMembers().contains(username))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving chat groups for user: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve chat groups for user", e);
        }
    }
}
