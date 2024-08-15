package com.remindifyapp.controller;

import com.remindifyapp.bean.ResponseDTO;
import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.service.AuthUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class AddMembersController {

    private static final Logger logger = LoggerFactory.getLogger(AddMembersController.class);

    private final AuthUserDetailsService userService;

    @Autowired
    public AddMembersController(AuthUserDetailsService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseDTO<List<AuthUser>> getUsersExcluding(@RequestParam(required = false) String username) {
        ResponseDTO<List<AuthUser>> responseDTO = new ResponseDTO<>();

        if (username == null || username.isEmpty()) {
            logger.info("Fetching all users as no username is provided.");
            List<AuthUser> users = userService.getAllUsers();
            responseDTO.setStatusCode(200);
            responseDTO.setMessage("Fetched all users successfully");
            responseDTO.setData(users);
        } else {
            logger.info("Fetching all users excluding the username: {}", username);
            List<AuthUser> users = userService.getAllUsersExcludingUsername(username);
            responseDTO.setStatusCode(200);
            responseDTO.setMessage("Fetched all users excluding the provided username successfully");
            responseDTO.setData(users);
        }

        return responseDTO;
    }
}
