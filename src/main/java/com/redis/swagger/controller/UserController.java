package com.redis.swagger.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.swagger.payload.response.UserResponse;
import com.redis.swagger.service.RedisService;
import com.redis.swagger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @GetMapping("/{username}")
    private ResponseEntity<?> getUser(@PathVariable("username") String username) throws JsonProcessingException {
        String keyWord = "user::";
        String json = (String) redisService.get(keyWord + username);
        if (json == null) {
            UserResponse userResponse = userService.userInfo(username);
            if(userResponse == null){
                throw new RuntimeException("User not found: " + username);
            }else{
                redisService.set(keyWord + username, objectMapper.writeValueAsString(userResponse));
                return new ResponseEntity<>(userResponse, HttpStatus.OK);
            }
        }
        UserResponse userResponse = objectMapper.readValue(json, UserResponse.class);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

}
