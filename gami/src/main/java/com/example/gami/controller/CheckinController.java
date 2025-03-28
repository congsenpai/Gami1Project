package com.example.gami.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gami.dto.ApiResponse;
import com.example.gami.dto.response.AttendanceResponse;
import com.example.gami.service.CheckInService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CheckinController {
    CheckInService checkInService;
    @PostMapping("/{userID}")
    public ApiResponse<Void> checkIn(@PathVariable String userID) {
        checkInService.userCheckIn(userID);
        return ApiResponse.<Void>builder()
        .message("User checkin succesfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }
    @GetMapping("/{userID}")
    public ApiResponse<List<AttendanceResponse>> getCheckinHistory(@PathVariable String userID) {
        return ApiResponse.<List<AttendanceResponse>>builder()
        .data(checkInService.getCheckInHistoryInMonth(userID))
        .message("Get monthly attendance sucessfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }
}
