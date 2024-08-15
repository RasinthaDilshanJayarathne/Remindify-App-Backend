/**
 * Author : rasintha_j
 * Date : 6/27/2024
 * Time : 10:03 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.util;

import java.util.Base64;

public class ImageUtils {
    public static byte[] decodeBase64ToImage(String base64String) {
        if (base64String.startsWith("data:image")) {
            base64String = base64String.substring(base64String.indexOf(",") + 1);
        }
        return Base64.getDecoder().decode(base64String);
    }
}
