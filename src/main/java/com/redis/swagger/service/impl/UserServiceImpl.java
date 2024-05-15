package com.redis.swagger.service.impl;

import com.redis.swagger.payload.response.UserResponse;
import com.redis.swagger.repository.UserRepository;
import com.redis.swagger.repository.UserRoleRepository;
import com.redis.swagger.service.UserService;
import com.redis.swagger.entity.Role;
import com.redis.swagger.entity.User;
import com.redis.swagger.entity.UserRole;
import com.redis.swagger.payload.request.LoginRequest;
import com.redis.swagger.payload.request.RegisterRequest;
import com.redis.swagger.payload.response.RoleResponse;
import com.redis.swagger.repository.RoleRepository;
import com.redis.swagger.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @Override
    public String login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            Set<UserRole> userRoles = userRepository.findByUsername(loginRequest.getUsername()).getUserRoles();
            return jwtTokenUtil.createToken(loginRequest.getUsername(), userRoles);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username/password supplied", e);
        }
    }

    @Override
    public String register(RegisterRequest registerRequest) {
        if (!userRepository.existsByUsername(registerRequest.getUsername())) {
            User user = modelMapper.map(registerRequest, User.class);
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setCreatedAt(new Timestamp(new Date().getTime()));
            user.setEnabled(true);
            Role role = roleRepository.findByName(("user").toUpperCase());
            User newUser = userRepository.save(user);
            UserRole userRole = new UserRole();
            userRole.setUser(newUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
            Optional<User> userOptional = userRepository.findById(newUser.getId());
            Set<UserRole> userRoles = userOptional.map(User::getUserRoles).orElse(Collections.emptySet());
            return jwtTokenUtil.createToken(registerRequest.getUsername(), userRoles);
        } else {
            throw new RuntimeException("Username is already in use");
        }
    }

    @Override
    public String refresh(String username) {
        return jwtTokenUtil.createToken(username, userRepository.findByUsername(username).getUserRoles());
    }

    @Override
    public UserResponse userInfo(String username) {
        User user = userRepository.findByUsername(username);
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setRoles(user.getUserRoles().stream().map(userRole -> new RoleResponse(userRole.getId(), userRole.getAuthority())).toList());
        return userResponse;
    }
}
