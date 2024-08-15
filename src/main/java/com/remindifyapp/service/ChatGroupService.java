/**
 * Author : rasintha_j
 * Date : 8/9/2024
 * Time : 2:27 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.service;

import com.remindifyapp.entity.ChatGroup;
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

    public ChatGroup createChatGroup(ChatGroup chatGroup) {
        chatGroup.setCreatedDate(LocalDateTime.now());
        chatGroup.setStatus("active");
        return chatGroupRepository.save(chatGroup);
    }

    public List<ChatGroup> getAllChatGroups() {
        return chatGroupRepository.findAll();
    }

    public Optional<ChatGroup> getChatGroupById(int id) {
        return chatGroupRepository.findById(id);
    }

    public List<ChatGroup> getChatGroupsByGroupName(String groupname) {
        return chatGroupRepository.findByGroupname(groupname);
    }

    public ChatGroup updateChatGroup(int id, ChatGroup updatedChatGroup) {
        return chatGroupRepository.findById(id)
                .map(chatGroup -> {
                    chatGroup.setGroupname(updatedChatGroup.getGroupname());
                    chatGroup.setMembers(updatedChatGroup.getMembers());
                    chatGroup.setStatus(updatedChatGroup.getStatus());
                    return chatGroupRepository.save(chatGroup);
                }).orElse(null);
    }

    public void deleteChatGroup(int id) {
        chatGroupRepository.deleteById(id);
    }

    public List<ChatGroup> getAllUsersIncludingUsername(String username) {
        List<ChatGroup> allGroups = chatGroupRepository.findAll();
        return allGroups.stream()
                .filter(group -> group.getMembers().contains(username))
                .collect(Collectors.toList());
    }
}
