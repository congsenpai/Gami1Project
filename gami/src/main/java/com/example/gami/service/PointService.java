package com.example.gami.service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gami.dto.request.Point.UsePointRequest;
import com.example.gami.dto.response.PointHistoryResponse;
import com.example.gami.enums.ActionType;
import com.example.gami.exception.AppException;
import com.example.gami.exception.ErrorCode;
import com.example.gami.mapper.PointHistoryMapper;
import com.example.gami.model.PointHistory;
import com.example.gami.model.User;
import com.example.gami.repository.PointHistoryRepository;
import com.example.gami.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PointService {
    RedissonClient redissonClient;
    RedisService redisService;
    UserRepository userRepository;
    PointHistoryRepository pointHistoryRepository;
    PointHistoryMapper pointHistoryMapper;

    @Transactional
    public void updatePoints(String userId, int points, boolean isAdding) {
        String lockKey = "points:lock:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Another process is modifying points for user: " + userId);
            }
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
                PointHistory pointHistory=PointHistory.builder()
                .action(isAdding?ActionType.LOGIN:ActionType.USEPOINT)
                .actionDate(LocalDate.now())
                .pointChange(points)
                .user(user)
                .build();
                int newPoints = isAdding ? user.getPoints() + points : user.getPoints() - points;
                if (newPoints < 0) {
                    throw new RuntimeException("Not enough points");
                }

                user.setPoints(newPoints);
                userRepository.save(user);
                pointHistoryRepository.save(pointHistory);


                // Cập nhật cache
                redisService.cachePointHistory(userId, pointHistoryMapper.toPointHistoryResponse(pointHistory));
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire lock", e);
        }
    }

    public void addPoints(String userId, int points) {
        updatePoints(userId, points, true);
    }

    public PointHistoryResponse deductPoints(UsePointRequest request) {
        updatePoints(request.getUserID(), request.getPoints(), false);
        PointHistory pointHistory=PointHistory.builder()
        .action(ActionType.USEPOINT)
        .actionDate(LocalDate.now())
        .pointChange(request.getPoints())
        .build();
        return pointHistoryMapper.toPointHistoryResponse(pointHistory);
    }


}
