package com.example.gami.exception;

public enum ErrorCode {

    // System errors
    ERROR_NOT_FOUND(0001,"Error special"),
    NOT_REFRESH_TOKEN(0002,"Not refresh token"),
    NOT_ACCESS_TOKEN(0003,"Not access token"),
    INVALID_TOKEN(0004,"Invalid token"),
    SLOT_NOT_VALID_WITH_VEHICLE_TYPE(0005,"Slot not valid with vehicle type"),
    



    // User errors
    USER_NOT_FOUND(1001, "User not found"),
    USER_ALREADY_EXISTS(1002, "User already exists"),
    PASSWORD_NOT_MATCH(1003, "Password not match"),
    PASSWORD_NOT_VALID(1004, "Password not valid"),
    INVALID_CREDENTIALS(1005, "Invalid credentials"),
    UNAUTHORIZED(1006, "Unauthorized"),
    INVALID_REQUEST(1007, "Invalid request"),
    INTERNAL_SERVER_ERROR(1008, "Internal server error"),
    USER_NOT_EXISTS(1009, "User not exists"),
    TOKEN_EXPIRED(1010, "Token expired"),
    USER_ALREADY_CHECKIN(1011,"User already checkin today"),
    NOT_HAVE_POINT_HISTORY_ON_THIS_DAY(1012,"User not have point history on this day"),
    MAX_CHECKIN_REACHED(0006,"Checking limited reach")


    ;


    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
