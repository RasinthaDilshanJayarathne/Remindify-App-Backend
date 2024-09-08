package com.remindifyapp.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatGroupRequest {
    private String groupName;
    private List<Member> members;
    private LocalDateTime createdDate;
    private String status;

    @Data
    public static class Member {
        private String id;
        private String username;
    }
}
