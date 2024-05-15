package com.redis.swagger.service;

import com.redis.swagger.payload.response.UserResponse;
import com.redis.swagger.payload.request.LoginRequest;
import com.redis.swagger.payload.request.RegisterRequest;

public interface UserService {
    String login(LoginRequest loginRequest);

    String register(RegisterRequest registerRequest);

    String refresh(String username);

    UserResponse userInfo(String username);

}
