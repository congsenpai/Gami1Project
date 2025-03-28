package com.example.gami.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.gami.dto.request.UserRequest;
import com.example.gami.dto.response.UserResponse;
import com.example.gami.exception.AppException;
import com.example.gami.exception.ErrorCode;
import com.example.gami.mapper.UserMapper;
import com.example.gami.model.User;
import com.example.gami.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RedisService redisService;
    public UserResponse createUser(UserRequest request){
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new AppException(ErrorCode.PASSWORD_NOT_VALID);
        }
        User user=userMapper.toUser(request);
        // Encoded Password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }
    public UserResponse getUserById(String id) {
        var userResponse=redisService.getUserProfile(id);
        if (userResponse==null) {
            var user = userRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            userResponse=userMapper.toUserResponse(user);
            redisService.cacheUserProfile(userResponse);
        }
        return userResponse;
    }

}
