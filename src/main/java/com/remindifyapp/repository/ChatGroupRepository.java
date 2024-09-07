/**
 * Author : rasintha_j
 * Date : 8/9/2024
 * Time : 2:20 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.repository;

import com.remindifyapp.entity.AuthUser;
import com.remindifyapp.entity.ChatGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatGroupRepository extends MongoRepository<ChatGroup, Integer> {
    List<ChatGroup> findByGroupname(String groupname);

    List<ChatGroup> findAllByUsername(String username);

    List<ChatGroup> findAllByEmail(String email);
}
