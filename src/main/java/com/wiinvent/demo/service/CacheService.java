package com.wiinvent.demo.service;

import com.wiinvent.demo.controller.dto.UserLotusDTO;
import com.wiinvent.demo.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CacheService {
    private static final Logger log = LogManager.getLogger(CacheService.class);
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @CachePut(cacheNames = "rc24h", key = "'CheckinInMonth:'+#userId")
    public long updateCheckinInMonth(Long userId, long checkinInMonth) {
        return checkinInMonth;
    }

    @CachePut(cacheNames = "rc24h", key = "'UserInfo:'+#userId")
    public UserLotusDTO updateUserInfo(Long userId, int extraPoint, String action) {
        UserLotusDTO userLotusDTO = userService.getUserLotusDTO(userId);
        log.info("updateUserInfo|UserId|" + userId + "|" + userLotusDTO.getLotusPoint() + "|Point|" + extraPoint);
        if (action.equals("add")) {
            userLotusDTO.setLotusPoint(userLotusDTO.getLotusPoint() + extraPoint);
        } else {
            userLotusDTO.setLotusPoint(userLotusDTO.getLotusPoint() - extraPoint);
        }
        return userLotusDTO;
    }

    @CacheEvict(cacheNames = "rc24h", key = "'ListAttendance:'+#userId")
    public void updateListAttendance(Long userId) {
    }

    public void removeCacheHistory(Long userId) {
        try {
            String keyPrefix = "rc24h::PointHistory:" + userId + "*";
            Set<String> keys = redisTemplate.keys(keyPrefix);
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("removeCacheHistory|UserId|" + userId + "|Exception|" + e.getMessage(), e);
        }
    }

    @CachePut(cacheNames = "rc24h", key = "'isUserExist:'+#userId")
    public boolean updateCacheUserExist(Long userId, boolean isExist) {
        return isExist;
    }

    @CachePut(cacheNames = "rc24h", key = "'UserInfo:'+#userId")
    public UserLotusDTO updateUserInfo(Long userId, int point) {
        UserLotusDTO userLotusDTO = userService.getUserLotusDTO(userId);
        userLotusDTO.setLotusPoint(point);
        return userLotusDTO;
    }
}
