package com.wiinvent.demo.service;

import com.wiinvent.demo.controller.UserController;
import com.wiinvent.demo.controller.dto.UserInfoDTO;
import com.wiinvent.demo.controller.dto.UserLotusDTO;
import com.wiinvent.demo.controller.dto.UserRequestDTO;
import com.wiinvent.demo.entity.User;
import com.wiinvent.demo.repo.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger log = LogManager.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;

    public boolean checkExistUser(String username) {
        return userRepository.existsByUsername(username);
    }

    @Cacheable(cacheNames = "rc24h", key = "'isUserExist:'+#userId")
    public boolean checkExistUser(Long userId) {
        return userRepository.existsById(userId);
    }

    public User saveUser(UserRequestDTO userRequestDTO) {
        try {
            User user = new User();
            user.setUsername(userRequestDTO.getUsername());
            user.setPassword(userRequestDTO.getPassword());
            user.setAvatar(userRequestDTO.getAvatar());
            user.setBirthday(userRequestDTO.getBirthday());
            user.setGender(userRequestDTO.getGender());
            user.setFullName(userRequestDTO.getFullName());
            user.setCountryCode(userRequestDTO.getCountryCode());
            user.setLanguageCode(userRequestDTO.getLanguageCode());
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("saveUser|Data|" + userRequestDTO + "|Exception|" + e.getMessage(), e);
            return null;
        }
    }

    public User getUserInfo(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    @Cacheable(cacheNames = "rc24h", key = "'UserInfo:'+#userId", unless = "#result==null")
    public UserLotusDTO getUserLotusDTO(Long userId) {
        User user = getUserInfo(userId);
        UserLotusDTO userLotusDTO = new UserLotusDTO();
        if (user != null) {
            userLotusDTO.setFullName(user.getFullName());
            userLotusDTO.setAvatar(user.getAvatar());
            userLotusDTO.setLotusPoint(user.getLotusPoint());
        } else {
            return null;
        }
        return userLotusDTO;
    }
}