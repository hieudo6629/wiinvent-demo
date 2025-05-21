package com.wiinvent.demo.controller;

import com.wiinvent.demo.controller.dto.JwtTokenDTO;
import com.wiinvent.demo.entity.User;
import com.wiinvent.demo.repo.UserRepository;
import com.wiinvent.demo.utils.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/without-bearer")
public class WithoutBearerController {
    private static final Logger log = LogManager.getLogger(WithoutBearerController.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/request-jwt")
    public ResponseEntity<?> requestJWT(@RequestParam("userId") Long userId,
                                        @RequestParam("timestamp") Long timestamp,
                                        @RequestParam("cert") String clientSecurity) {
        try {
            long start = System.currentTimeMillis();
            JwtTokenDTO jwtTokenDTO = new JwtTokenDTO();
            if (start < timestamp) {
                log.warn("requestJWT|UserId|" + userId + "|Timestamp|" + timestamp + "|Timestamp Invalid");
                jwtTokenDTO.setStatusCode(400);
                jwtTokenDTO.setDesc("Timestamp Invalid!");
                jwtTokenDTO.setData("");
                return ResponseEntity.badRequest().body(jwtTokenDTO);
            }
            if (start - timestamp > 60000) {
                log.warn("requestJWT|UserId|" + userId + "|Timestamp|" + timestamp + "|Request Expired|" + (start - timestamp));
                jwtTokenDTO.setStatusCode(400);
                jwtTokenDTO.setDesc("Your request has been expired!");
                jwtTokenDTO.setData("");
                return ResponseEntity.badRequest().body(jwtTokenDTO);
            }
            String paramCheck = String.valueOf(userId + timestamp);
            if (passwordEncoder.matches(paramCheck, clientSecurity)) {
                log.warn("requestJWT|UserId|" + userId + "|Timestamp|" + timestamp + "|Invalid Security");
                jwtTokenDTO.setStatusCode(400);
                jwtTokenDTO.setDesc("Invalid Security!");
                jwtTokenDTO.setData("");
                return ResponseEntity.badRequest().body(jwtTokenDTO);
            }
            User user = userRepository.findById(userId).get();
            String token = jwtUtil.generateToken(userId, user.getUsername(), user.getPhoneNumber());
            jwtTokenDTO.setStatusCode(200);
            jwtTokenDTO.setDesc("Successful!");
            jwtTokenDTO.setData(token);
            return ResponseEntity.ok().body(jwtTokenDTO);
        } catch (Exception e) {
            log.error("requestJWT|UserId|" + userId + "|Timestamp|" + timestamp + "|Exception|" + e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal error, please try again later!");
        }
    }
}
