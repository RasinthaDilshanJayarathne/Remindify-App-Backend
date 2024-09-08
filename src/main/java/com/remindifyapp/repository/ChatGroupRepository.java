/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 8:01 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.repository;

import com.remindifyapp.entity.ChatGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatGroupRepository extends MongoRepository<ChatGroup, String> {
}
