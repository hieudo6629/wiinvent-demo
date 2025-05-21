package com.wiinvent.demo.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class PhoneNumberValidate {
    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    public static boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        try {
            return phoneUtil.isValidNumber(phoneUtil.parse(phoneNumber, countryCode));
        } catch (NumberParseException e) {
            return false;
        }
    }

}
