package com.remindifyapp.controller;

import com.remindifyapp.entity.*;
import com.remindifyapp.repository.AuthUserRepository;
import com.remindifyapp.service.AuthUserDetailsService;
import com.remindifyapp.service.ChatGroupService;
import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.service.JWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/groups")
public class ChatGroupController {

    private static final Logger logger = LoggerFactory.getLogger(ChatGroupController.class);

    @Autowired
    private ChatGroupService chatGroupService;

    @Autowired
    private AuthUserDetailsService authUserDetailsService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthUserRepository authUserRepository;


    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<ChatGroup>> createChatGroup(@Valid @RequestBody ChatGroupRequest chatGroupRequest, @RequestHeader("Authorization") String token) {

        logger.info("Received request to create chat group: {} with users: {}", chatGroupRequest.getGroupName(), chatGroupRequest.getMembers());
        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();
        String username;

        try {
            // Extract and validate the username from the token
            username = jwtService.extractUsername(token);
            Optional<AuthUser> creatorOptional = authUserRepository.findByUsername(username);
            if (!creatorOptional.isPresent() || !jwtService.isTokenValid(token, creatorOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            AuthUser creator = creatorOptional.get();

            // Extract and validate user IDs from the request
            List<String> userIds = chatGroupRequest.getMembers().stream().map(ChatGroupRequest.Member::getId).collect(Collectors.toList());
            List<AuthUser> validUsers = authUserDetailsService.findUsersByIds(userIds);

            // Check if all provided user IDs are valid
            if (validUsers.size() != userIds.size()) {
                responseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
                responseDTO.setMessage("One or more user IDs are invalid or do not exist");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
            }

            // Add the creator to the list of members
            validUsers.add(creator);

            // Create and save the chat group
            ChatGroup chatGroup = ChatGroup.builder().groupname(chatGroupRequest.getGroupName()).members(validUsers).createdDate(chatGroupRequest.getCreatedDate()).status(chatGroupRequest.getStatus()).build();

            ChatGroup createdChatGroup = chatGroupService.createChatGroup(chatGroup);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Chat group created successfully");
            responseDTO.setData(createdChatGroup);

            logger.info("Chat group created successfully: {}", createdChatGroup);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Exception e) {
            logger.error("Error creating chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while creating the chat group");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    @PostMapping("/addUser/{groupId}/{userId}")
    public ResponseEntity<ResponseDTO<ChatGroup>> addUserToGroup(@PathVariable("groupId") String groupId, @PathVariable("userId") String userId, @RequestHeader("Authorization") String token) {

        logger.info("Received request to add user {} to group {}", userId, groupId);
        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();
        String username;
        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            // Add user to chat group
            ChatGroup updatedChatGroup = chatGroupService.addUserToGroup(groupId, userId);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("User added successfully");
            responseDTO.setData(updatedChatGroup);

            logger.info("User {} added successfully to group {}", userId, groupId);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error adding user to chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while adding the user to the chat group");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    @PostMapping("/removeUser/{groupId}/{userId}")
    public ResponseEntity<ResponseDTO<ChatGroup>> removeUserFromGroup(@PathVariable("groupId") String groupId, @PathVariable("userId") String userId, @RequestHeader("Authorization") String token) {

        logger.info("Received request to remove user with ID: {} from chat group with ID: {}", userId, groupId);
        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();
        String username;
        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            ChatGroup updatedChatGroup = chatGroupService.removeUserFromGroup(groupId, userId);
            if (updatedChatGroup == null) {
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("Chat group not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO);
            }

            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("User removed successfully");
            responseDTO.setData(updatedChatGroup);

            logger.info("User with ID: {} removed successfully from chat group with ID: {}", userId, groupId);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error removing user from chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while removing the user from the chat group");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<ChatGroup>>> getAllChatGroups(@RequestHeader("Authorization") String token) {

        logger.info("Received request to fetch all chat groups");

        ResponseDTO<List<ChatGroup>> responseDTO = new ResponseDTO<>();
        String username;
        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            List<ChatGroup> chatGroups = chatGroupService.getAllChatGroups();
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Fetched all chat groups successfully");
            responseDTO.setData(chatGroups);

            logger.info("Fetched {} chat groups", chatGroups.size());
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error fetching chat groups: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching chat groups");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    @GetMapping("/my-groups")
    public ResponseEntity<ResponseDTO<List<ChatGroup>>> getMyChatGroups(@RequestHeader("Authorization") String token) {

        logger.info("Received request to fetch chat groups where the logged-in user is a member");

        ResponseDTO<List<ChatGroup>> responseDTO = new ResponseDTO<>();
        String username;
        try {
            username = jwtService.extractUsername(token);

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            // Fetch chat groups where the logged-in user is a member
            List<ChatGroup> userChatGroups = chatGroupService.getChatGroupsForUser(username);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Fetched chat groups where the user is a member successfully");
            responseDTO.setData(userChatGroups);

            logger.info("Fetched {} chat groups for user: {}", userChatGroups.size(), username);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error fetching chat groups for user {}: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching chat groups");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    @GetMapping("/allUsers/{groupId}")
    public ResponseEntity<ResponseDTO<ChatGroup>> getGroupDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable String groupId) {

        logger.info("Received request to fetch details for group with ID: {}", groupId);

        ResponseDTO<ChatGroup> responseDTO = new ResponseDTO<>();
        String username;
        try {
            username = jwtService.extractUsername(token);
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("Username extraction failed");
            }

            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("User not found");
                logger.warn("User not found for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            if (!jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            // Fetch the group by its ID
            Optional<ChatGroup> groupOptional = chatGroupService.getChatGroupById(groupId);
            if (!groupOptional.isPresent()) {
                responseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
                responseDTO.setMessage("Group not found");
                logger.warn("Group not found for ID: {}", groupId);
                return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
            }

            ChatGroup chatGroup = groupOptional.get();

            // Prepare the DTO with group details and members
            ChatGroup chatGroupDTO = new ChatGroup();
            chatGroupDTO.setGroupname(chatGroup.getGroupname());
            chatGroupDTO.setMembers(chatGroup.getMembers());

            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Fetched group details successfully");
            responseDTO.setData(chatGroupDTO);

            logger.info("Fetched details for group with ID: {}", groupId);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error fetching details for group with ID: {}: ", groupId, e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching group details");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }


    // Add a new message to a chat group
    @PostMapping("/{groupId}/messages")
    public ResponseEntity<ResponseDTO<Message>> addMessageToGroup(
            @PathVariable("groupId") String groupId,
            @Valid @RequestBody Message message,
            @RequestHeader("Authorization") String token) {

        logger.info("Received request to add message to group {}", groupId);
        ResponseDTO<Message> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);
            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            message.setSenderId(userOptional.get().getId());
            Message savedMessage = chatGroupService.addMessageToGroup(groupId, message);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Message added successfully");
            responseDTO.setData(savedMessage);

            logger.info("Message added successfully to group {}", groupId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Exception e) {
            logger.error("Error adding message to chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while adding the message");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    // Get all messages from a chat group
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<ResponseDTO<List<Message>>> getMessagesFromGroup(
            @PathVariable("groupId") String groupId,
            @RequestHeader("Authorization") String token) {

        logger.info("Received request to fetch messages from group {}", groupId);
        ResponseDTO<List<Message>> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);
            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            List<Message> messages = chatGroupService.getMessagesFromGroup(groupId);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Fetched messages successfully");
            responseDTO.setData(messages);

            logger.info("Fetched {} messages from group {}", messages.size(), groupId);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error fetching messages from chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching messages");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    // Add a new reminder to a chat group
    @PostMapping("/{groupId}/reminders")
    public ResponseEntity<ResponseDTO<Reminder>> addReminderToGroup(
            @PathVariable("groupId") String groupId,
            @Valid @RequestBody Reminder reminder,
            @RequestHeader("Authorization") String token) {

        logger.info("Received request to add reminder to group {}", groupId);
        ResponseDTO<Reminder> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);
            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            reminder.setCreatedBy(userOptional.get().getId());
            Reminder savedReminder = chatGroupService.addReminderToGroup(groupId, reminder);
            responseDTO.setStatusCode(HttpStatus.CREATED.value());
            responseDTO.setMessage("Reminder added successfully");
            responseDTO.setData(savedReminder);

            logger.info("Reminder added successfully to group {}", groupId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Exception e) {
            logger.error("Error adding reminder to chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while adding the reminder");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    // Get all reminders from a chat group
    @GetMapping("/{groupId}/reminders")
    public ResponseEntity<ResponseDTO<List<Reminder>>> getRemindersFromGroup(
            @PathVariable("groupId") String groupId,
            @RequestHeader("Authorization") String token) {

        logger.info("Received request to fetch reminders from group {}", groupId);
        ResponseDTO<List<Reminder>> responseDTO = new ResponseDTO<>();
        String username;

        try {
            username = jwtService.extractUsername(token);
            Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
            if (!userOptional.isPresent() || !jwtService.isTokenValid(token, userOptional.get())) {
                responseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                responseDTO.setMessage("Invalid or expired token");
                logger.warn("Invalid or expired token for username: {}", username);
                return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
            }

            List<Reminder> reminders = chatGroupService.getRemindersFromGroup(groupId);
            responseDTO.setStatusCode(HttpStatus.OK.value());
            responseDTO.setMessage("Fetched reminders successfully");
            responseDTO.setData(reminders);

            logger.info("Fetched {} reminders from group {}", reminders.size(), groupId);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Error fetching reminders from chat group: ", e);
            responseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDTO.setMessage("An error occurred while fetching reminders");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }
}
