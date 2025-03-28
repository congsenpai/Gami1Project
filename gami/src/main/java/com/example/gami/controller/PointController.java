package com.example.gami.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gami.dto.ApiResponse;
import com.example.gami.dto.request.Point.UsePointRequest;
import com.example.gami.dto.response.PointHistoryResponse;
import com.example.gami.service.PointHistoryService;
import com.example.gami.service.PointService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class PointController {
    PointService pointService;
    PointHistoryService pointHistoryService;
    @PostMapping("deduct")
    public ApiResponse<PointHistoryResponse> usePoint(@RequestBody @Valid UsePointRequest request) {
        return ApiResponse.<PointHistoryResponse>builder()
        .data(pointService.deductPoints(request))
        .message("Use point sucessfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }
    @GetMapping("/{userID}")
    public ApiResponse<List<PointHistoryResponse>> getPointHistory(@PathVariable String userID) {
        return ApiResponse.<List<PointHistoryResponse>>builder()
        .data(pointHistoryService.getTodayPointHistory(userID))
        .message("Get point sucessfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }
}
