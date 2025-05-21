package com.wiinvent.demo.controller;

import com.wiinvent.demo.controller.dto.MarkAttendanceDTO;
import com.wiinvent.demo.controller.dto.UserInfoDTO;
import com.wiinvent.demo.controller.dto.UserLotusDTO;
import com.wiinvent.demo.controller.dto.UserRequestDTO;
import com.wiinvent.demo.entity.User;
import com.wiinvent.demo.service.CacheService;
import com.wiinvent.demo.service.UserService;
import com.wiinvent.demo.utils.PhoneNumberValidate;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    private static final Logger log = LogManager.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CacheService cacheService;

    @GetMapping("/test")
    public String test() {
        return "OK";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        try {
            long start = System.currentTimeMillis();
            log.info("registerUser|Data|{}|START", userRequestDTO);
            if (userService.checkExistUser(userRequestDTO.getUsername())) {
                return ResponseEntity.badRequest().body(new MarkAttendanceDTO(400, "Username does exist!"));
            }
            if (!PhoneNumberValidate.isValidPhoneNumber(userRequestDTO.getPhoneNumber(), userRequestDTO.getCountryCode())) {
                return ResponseEntity.badRequest().body(new MarkAttendanceDTO(400, "Phone number invalid!"));
            }
            String rawEncrypt = userRequestDTO.getUsername() + userRequestDTO.getPassword();
            if (!passwordEncoder.matches(rawEncrypt, userRequestDTO.getAuthen())) {
                return ResponseEntity.badRequest().body(new MarkAttendanceDTO(400, "Register information invalid!"));
            }
            String passwordEncrypted = passwordEncoder.encode(userRequestDTO.getPassword());
            userRequestDTO.setPassword(passwordEncrypted);
            User result = userService.saveUser(userRequestDTO);
            long proc = System.currentTimeMillis() - start;
            log.info("registerUser|Data|{}|ExecuteTime|{}", userRequestDTO, proc);
            if (result != null) {
                cacheService.updateCacheUserExist(result.getId(), true);
                return ResponseEntity.ok().body(new MarkAttendanceDTO(200, "Register successful!"));
            } else {
                return ResponseEntity.internalServerError().body(new MarkAttendanceDTO(500, "Internal error, please try again later!"));
            }
        } catch (Exception e) {
            log.error("registerUser|Data|{}|Exception|{}|{}", userRequestDTO, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new MarkAttendanceDTO(500, "Internal error, please try again later!"));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestParam("userId") Long userId) {
        try {
            long start = System.currentTimeMillis();
            log.info("getUserInfo|UserId|" + userId + "|START");
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            UserLotusDTO userLotusDTO = userService.getUserLotusDTO(userId);
            if (userLotusDTO != null) {
                userInfoDTO.setStatusCode(200);
                userInfoDTO.setDesc("success");
                userInfoDTO.setData(userLotusDTO);
            } else {
                userInfoDTO.setStatusCode(404);
                userInfoDTO.setDesc("User not found or does not exist!");
                userInfoDTO.setData(new UserLotusDTO());
            }
            long proc = System.currentTimeMillis() - start;
            log.info("getUserInfo|UserId|" + userId + "|ExecuteTime|" + proc);
            return ResponseEntity.ok().body(userInfoDTO);
        } catch (Exception e) {
            log.error("getUserInfo|UserId|" + userId + "|Exception|" + e.getMessage(), e);
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setStatusCode(500);
            userInfoDTO.setDesc("Internal error, please try again later!");
            userInfoDTO.setData(new UserLotusDTO());
            return ResponseEntity.internalServerError().body(userInfoDTO);
        }
    }
}
