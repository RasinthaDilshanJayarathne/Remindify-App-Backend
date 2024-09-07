/**
 * Author : rasintha_j
 * Date : 6/26/2024
 * Time : 8:01 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.bean;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    private String email;
    private String username;
    private String password;
    private Date birthday;
    private String image;
}
