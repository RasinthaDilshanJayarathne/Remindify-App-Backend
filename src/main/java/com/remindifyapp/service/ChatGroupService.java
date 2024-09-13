/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 8:02 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.service;

import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.entity.ChatGroup;
import com.remindifyapp.entity.Message;
import com.remindifyapp.entity.Reminder;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.repository.ChatGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatGroupService {
    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Autowired
    private AuthUserRepository authUserRepository;  // To fetch user details

    public ChatGroup createChatGroup(ChatGroup chatGroup) {
        return chatGroupRepository.save(chatGroup);
    }

    public ChatGroup removeUserFromGroup(String groupId, String userId) {
        Optional<ChatGroup> optionalChatGroup = chatGroupRepository.findById(groupId);
        if (optionalChatGroup.isPresent()) {
            ChatGroup chatGroup = optionalChatGroup.get();
            chatGroup.getMembers().removeIf(user -> user.getId().equals(userId));
            return chatGroupRepository.save(chatGroup);
        }
        return null; // Handle case where the chat group does not exist
    }

    public ChatGroup addUserToGroup(String groupId, String userId) throws Exception {
        Optional<ChatGroup> chatGroupOptional = chatGroupRepository.findById(groupId);
        if (!chatGroupOptional.isPresent()) {
            throw new Exception("Chat group not found");
        }

        Optional<AuthUser> userOptional = authUserRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }

        ChatGroup chatGroup = chatGroupOptional.get();
        AuthUser user = userOptional.get();

        // Check if user is already a member
        if (chatGroup.getMembers().stream().anyMatch(member -> member.getId().equals(userId))) {
            throw new Exception("User is already a member of the group");
        }

        chatGroup.getMembers().add(user);
        return chatGroupRepository.save(chatGroup);
    }

    public List<ChatGroup> getAllChatGroups() {
        return chatGroupRepository.findAll();
    }


    public Message addMessageToGroup(String groupId, Message message) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("ChatGroup not found"));
        chatGroup.getMessages().add(message);
        chatGroupRepository.save(chatGroup);
        return message;
    }

    public List<Message> getMessagesFromGroup(String groupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("ChatGroup not found"));
        return chatGroup.getMessages();
    }

    public Reminder addReminderToGroup(String groupId, Reminder reminder) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("ChatGroup not found"));
        chatGroup.getReminders().add(reminder);
        chatGroupRepository.save(chatGroup);
        return reminder;
    }

    public List<Reminder> getRemindersFromGroup(String groupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("ChatGroup not found"));
        return chatGroup.getReminders();
    }

    public List<ChatGroup> getChatGroupsForUser(String username) {
        return chatGroupRepository.findAll().stream()
                .filter(group -> group.getMembers().stream()
                        .anyMatch(member -> member.getUsername().equals(username)))
                .collect(Collectors.toList());
    }

    public Optional<ChatGroup> getChatGroupById(String groupId) {
        return chatGroupRepository.findById(groupId);
    }
}
