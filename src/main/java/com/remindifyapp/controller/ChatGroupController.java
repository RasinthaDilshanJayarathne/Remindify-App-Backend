package com.remindifyapp.controller;

import com.remindifyapp.entity.ChatGroup;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.entity.ChatGroupRequest;
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
}
