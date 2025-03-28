package com.example.gami.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.gami.dto.ApiResponse;


@ControllerAdvice
public class GlobalHandlerException {

    @SuppressWarnings("rawtypes")
    private ResponseEntity<ApiResponse> buildResponse(int code, String message) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }
    
    @SuppressWarnings({"rawtypes"})
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException e) {
        if (e instanceof AppException appException) {
            return handleAppException(appException); // Delegate
        }
        return buildResponse(ErrorCode.ERROR_NOT_FOUND.getCode(), e.getMessage());
    }
    @SuppressWarnings("rawtypes")
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildResponse(ErrorCode.ERROR_NOT_FOUND.getCode(), e.getMessage());
    }
    
    @SuppressWarnings("rawtypes")
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException e) {
        return buildResponse(e.getErrorCode().getCode(), e.getMessage());
    }
    
}
