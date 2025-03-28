package com.example.gami.config;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.gami.exception.AppException;
import com.example.gami.exception.ErrorCode;
import com.example.gami.model.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.signerKey}")
    protected String SECRET_KEY;
    
    @SuppressWarnings("UseSpecificCatch")
    public String generateAccessToken(User user){
        JWSHeader JWSHead =  new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet JWTClaimsSet = new JWTClaimsSet.Builder()
            .subject(user.getUsername())
            .issuer("gamiapp")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
            .claim("scope", buildString(user))
            .jwtID(UUID.randomUUID().toString())
            .claim("userId", user.getUserID())
            .build();
        Payload payload = new Payload(JWTClaimsSet.toJSONObject());
        JWSObject JWSObject = new JWSObject(JWSHead, payload);
        try {
            JWSObject.sign(new MACSigner(SECRET_KEY));
            return JWSObject.serialize();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR); // Trả lỗi chung nếu tạo token thất bại
        }
    }
    @SuppressWarnings("UseSpecificCatch")
    public String generateRefreshToken(User user){
        JWSHeader JWSHead =  new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet JWTClaimsSet = new JWTClaimsSet.Builder()
            .subject(user.getUsername())
            .issuer("gamiapp")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
            .claim("token-type", "refresh")
            .jwtID(UUID.randomUUID().toString())
            .claim("userId", user.getUserID())
            .build();
        Payload payload = new Payload(JWTClaimsSet.toJSONObject());
        JWSObject JWSObject = new JWSObject(JWSHead, payload);
        try {
            JWSObject.sign(new MACSigner(SECRET_KEY));
            return JWSObject.serialize();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR); // Trả lỗi chung nếu tạo token thất bại
        }
    }
    

    private String buildString(User user){
        return "User";
    }
}
