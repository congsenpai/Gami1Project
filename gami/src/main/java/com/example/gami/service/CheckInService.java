package com.example.gami.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gami.dto.response.AttendanceResponse;
import com.example.gami.enums.ActionType;
import com.example.gami.exception.AppException;
import com.example.gami.exception.ErrorCode;
import com.example.gami.mapper.CheckInMapper;
import com.example.gami.mapper.PointHistoryMapper;
import com.example.gami.model.CheckIn;
import com.example.gami.model.PointHistory;
import com.example.gami.repository.CheckInRepository;
import com.example.gami.repository.PointHistoryRepository;
import com.example.gami.repository.UserRepository;
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
public class CheckInService {
    UserRepository userRepository;
    RedissonClient redissonClient;
    RedisService redisService;
    CheckInRepository checkInRepository;
    CheckInMapper checkInMapper;
    ObjectMapper objectMapper;
    PointService pointService;
    PointHistoryRepository pointHistoryRepository;
    PointHistoryMapper pointHistoryMapper;
    
    @Transactional
    public void userCheckIn(String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        String lockKey = "checkin:lock:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
    
        try {
            // Thử lấy lock trong 5 giây, timeout sau 10 giây
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    LocalDate today = LocalDate.now();
                    LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                    LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
    
                    // Kiểm tra nếu user đã điểm danh hôm nay
                    if (checkInRepository.existsByUser_UserIDAndCheckinDate(userId, today)) {
                        throw new AppException(ErrorCode.USER_ALREADY_CHECKIN);
                    }
    
                    // Kiểm tra số lần điểm danh trong tháng
                    long checkInCountThisMonth = checkInRepository.countByUser_UserIDAndCheckinDateBetween(userId, firstDayOfMonth, lastDayOfMonth);
                    if (checkInCountThisMonth >= 7) {
                        throw new AppException(ErrorCode.MAX_CHECKIN_REACHED);
                    }
    
                    // Tính toán số điểm theo Fibonacci
                    int points = getFibonacciPoints((int) checkInCountThisMonth + 1);
    
                    // Persist vào DB trước
                    CheckIn checkIn = CheckIn.builder()
                            .checkinDate(today)
                            .user(user)
                            .pointsAwarded(points)
                            .build();
                    checkInRepository.save(checkIn);
    
                    // Sau khi lưu vào DB thành công, cache lại lịch sử điểm danh
                    redisService.cacheCheckInHistory(userId, checkInMapper.toAttendanceResponse(checkIn));
    
                    // Cộng điểm cho User
                    pointService.addPoints(userId, points);
                    PointHistory pointHistory = PointHistory.builder()
                            .action(ActionType.LOGIN)
                            .actionDate(today)
                            .pointChange(points)
                            .user(user)
                            .build();
                    
                    // Lưu vào DB và cache lại lịch sử điểm
                    pointHistoryRepository.save(pointHistory);
                    redisService.cachePointHistory(userId, pointHistoryMapper.toPointHistoryResponse(pointHistory));
    
                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("Another check-in process is running for user: " + userId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire lock", e);
        }
    }
    

    private int getFibonacciPoints(int day) {
        int[] fibonacci = {1, 2, 3, 5, 8, 13, 21};
        return (day <= 7) ? fibonacci[day - 1] : 0;
    }

    public List<AttendanceResponse> getCheckInHistoryInMonth(String userID) {
        List<AttendanceResponse> attendanceList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        int daysInMonth = today.lengthOfMonth(); // Số ngày trong tháng hiện tại
    
        // Khởi tạo danh sách với đủ số ngày trong tháng
        for (int day = 1; day <= daysInMonth; day++) {
            AttendanceResponse response = new AttendanceResponse();
            response.setCheckinDate(firstDayOfMonth.plusDays(day - 1));
            response.setCheckIn(false); // Mặc định là chưa check-in
            response.setPointsAwarded(0); // Mặc định không có điểm
            attendanceList.add(response);
        }
    
        // Kiểm tra cache trước
        RKeys keys = redissonClient.getKeys();
        String pattern = "user:attendance:" + userID + ":*";
        Iterable<String> cacheKeys = keys.getKeysByPattern(pattern);
    
        for (String key : cacheKeys) {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String json = bucket.get();
            if (json != null) {
                try {
                    AttendanceResponse response = objectMapper.readValue(json, AttendanceResponse.class);
                    int index = response.getCheckinDate().getDayOfMonth() - 1; // Tìm index tương ứng
                    attendanceList.set(index, response);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse check-in history from cache", e);
                }
            }
        }
    
        // Nếu cache chưa có đủ dữ liệu, lấy từ database
        LocalDate lastDayOfMonth = today.withDayOfMonth(daysInMonth);
        List<CheckIn> checkIns = checkInRepository.findByUser_UserIDAndCheckinDateBetween(userID, firstDayOfMonth, lastDayOfMonth);
        for (CheckIn checkIn : checkIns) {
            if (checkIn!=null) {
                int index = checkIn.getCheckinDate().getDayOfMonth() - 1;
            AttendanceResponse response = new AttendanceResponse();
            response.setPointsAwarded(checkIn.getPointsAwarded());
            response.setCheckinDate(checkIn.getCheckinDate());
            response.setCheckIn(true);
    
            // Cache lại dữ liệu
            redisService.cacheCheckInHistory(userID, response);
            attendanceList.set(index, response); // Cập nhật lại list
            }
            
        }
    
        return attendanceList;
    }
    
}
