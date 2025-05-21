package com.wiinvent.demo.controller.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;


public class UserRequestDTO {
    @NotBlank
    @Size(min = 8, max = 32)
    private String username;
    @NotBlank
    private String fullName;
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự trở lên!")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "Mật khẩu phải có ít nhất một chữ hoa, số và ký tự đặc biệt!")
    private String password;
    @NotBlank
    private String avatar;
    @NotBlank
    private String phoneNumber;
    @Past(message = "Ngày sinh phải là ngày trong quá khứ!")
    private LocalDate birthday;
    @Min(value = 0, message = "Giới tính không hợp lệ!")
    @Max(value = 2, message = "Giới tính không hợp lệ!")
    private int gender; // 0 - nam, 1 - nữ, 2 other
    @NotBlank
    private String authen; // BCrypt (username + password)
    @NotBlank
    private String languageCode;
    @NotBlank
    private String countryCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAuthen() {
        return authen;
    }

    public void setAuthen(String authen) {
        this.authen = authen;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
