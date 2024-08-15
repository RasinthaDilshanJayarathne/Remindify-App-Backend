/**
 * Author : rasintha_j
 * Date : 6/27/2024
 * Time : 4:10 PM
 * Project Name : remindifyapp
 */

package com.remindifyapp.bean;

import lombok.Data;

@Data
public class ResponseDTO<T> {
    private int statusCode;
    private String message;
    private Object data;
}
