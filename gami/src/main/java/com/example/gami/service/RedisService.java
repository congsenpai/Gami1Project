package com.example.gami.service;

import java.time.Duration;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.example.gami.dto.response.AttendanceResponse;
import com.example.gami.dto.response.PointHistoryResponse;
import com.example.gami.dto.response.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {
    RedissonClient redissonClient;
    ObjectMapper objectMapper;

    public void cacheUserProfile(UserResponse userResponse) {
        String key = "user:profile:" + userResponse.getUserID();
        RBucket<String> bucket = redissonClient.getBucket(key);

        try {
            String jsonValue = objectMapper.writeValueAsString(userResponse);
            bucket.set(jsonValue, Duration.ofMinutes(10)); // Cache trong 10 phút
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing user profile", e);
        }
    }

    public UserResponse getUserProfile(String userID) {
        String key = "user:profile:" + userID;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String jsonValue = bucket.get();

        if (jsonValue != null) {
            try {
                return objectMapper.readValue(jsonValue, UserResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error deserializing user profile", e);
            }
        }
        return null;
    }
    public void cacheCheckInHistory(String userID, AttendanceResponse attendance) {
        String key = "user:attendance:" + userID + ":" + attendance.getCheckinDate();
        RBucket<String> bucket = redissonClient.getBucket(key);
        try {
            String json = objectMapper.writeValueAsString(attendance);
            bucket.set(json, Duration.ofDays(30)); // Cache trong 30 ngày
            log.info(" Cached check-in history for user {} on {}", userID, attendance.getCheckinDate());
        } catch (JsonProcessingException e) {
            log.error(" Failed to cache check-in history", e);
        }
    }

    public void cachePointHistory(String userID, PointHistoryResponse history) {
        String key = "user:pointHistory:" + userID + ":" + history.getActionDate();
        RBucket<String> bucket = redissonClient.getBucket(key);
        try {
            String json = objectMapper.writeValueAsString(history);
            bucket.set(json, Duration.ofDays(1)); // Cache trong 1 ngày
            log.info(" Cached point history for user {} - action: {}", userID, history.getAction());
        } catch (JsonProcessingException e) {
            log.error(" Failed to cache point history", e);
        }
    }


}
