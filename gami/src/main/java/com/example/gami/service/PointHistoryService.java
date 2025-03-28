package com.example.gami.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.example.gami.dto.response.PointHistoryResponse;
import com.example.gami.model.PointHistory;
import com.example.gami.repository.PointHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PointHistoryService {
    ObjectMapper objectMapper;
    RedissonClient redissonClient;
    PointHistoryRepository pointHistoryRepository;
    
    public List<PointHistoryResponse> getTodayPointHistory(String userID) {
        LocalDate today = LocalDate.now();
        String cacheKey = "user:pointHistory:" + userID + ":" + today;

        // Kiểm tra cache trước
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        String json = bucket.get();
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<PointHistoryResponse>>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to parse point history from cache", e);
            }
        }

        // Nếu không có trong cache, truy vấn DB
        List<PointHistory> dbHistory = pointHistoryRepository.findByUser_UserIDAndActionDate(userID, today);

        // Chuyển đổi sang DTO
        List<PointHistoryResponse> historyList = dbHistory.stream()
                .map(p -> new PointHistoryResponse(p.getAction(), p.getPointChange(), p.getActionDate()))
                .collect(Collectors.toList());

        // Cache lại kết quả
        try {
            bucket.set(objectMapper.writeValueAsString(historyList), Duration.ofDays(1));
        } catch (JsonProcessingException e) {
            log.error("Failed to cache point history", e);
        }

        return historyList;
    }

}
