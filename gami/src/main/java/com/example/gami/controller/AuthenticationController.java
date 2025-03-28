package com.example.gami.controller;
import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gami.dto.ApiResponse;
import com.example.gami.dto.request.Authentication.AuthenticationRequest;
import com.example.gami.dto.request.Authentication.IntrospectRequest;
import com.example.gami.dto.request.Authentication.LogoutRequest;
import com.example.gami.dto.request.Authentication.RefreshRequest;
import com.example.gami.dto.response.AuthenticationResponse;
import com.example.gami.dto.response.IntrospectResponse;
import com.example.gami.service.AuthenticationSevice;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationSevice authenticationSevice;
    public AuthenticationController(AuthenticationSevice authenticationSevice) {
        this.authenticationSevice = authenticationSevice;
    }
    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        var result = authenticationSevice.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
        .data(result)
        .message("User login successfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
    throws JOSEException, ParseException {
        var result = authenticationSevice.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
        .data(result)
        .message("Checked successfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws JOSEException, ParseException {
        authenticationSevice.logout(request);
        return ApiResponse.<Void>builder()
        .message("Logout successfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        var result = authenticationSevice.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
        .data(result)
        .message("Refresh successfully")
        .statusCode(HttpStatus.ACCEPTED)
        .build();
    }

}
