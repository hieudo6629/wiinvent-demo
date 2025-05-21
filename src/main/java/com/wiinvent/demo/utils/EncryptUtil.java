package com.wiinvent.demo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {
    public static String encryptMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());

            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (EnumConstantNotPresentException e) {
//            logger.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
//            logger.error(e.getMessage(), e);
        }
        return "";
    }
}
