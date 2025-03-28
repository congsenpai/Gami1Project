package com.example.gami.service;

import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.gami.config.JwtTokenProvider;
import com.example.gami.dto.request.Authentication.AuthenticationRequest;
import com.example.gami.dto.request.Authentication.IntrospectRequest;
import com.example.gami.dto.request.Authentication.LogoutRequest;
import com.example.gami.dto.request.Authentication.RefreshRequest;
import com.example.gami.dto.response.AuthenticationResponse;
import com.example.gami.dto.response.IntrospectResponse;
import com.example.gami.exception.AppException;
import com.example.gami.exception.ErrorCode;
import com.example.gami.model.InvalidToken;
import com.example.gami.model.User;
import com.example.gami.repository.InvalidatedRepository;
import com.example.gami.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationSevice {
    final UserRepository userReponsitory;
    final PasswordEncoder passwordEncoder;
    final JwtTokenProvider jwtTokenProvider;
    final InvalidatedRepository invalidatedRepository;

    @Value("${jwt.signerKey}")
    protected String SECRET_KEY;


    public IntrospectResponse introspect(IntrospectRequest request)
    throws JOSEException, ParseException{
        boolean isValid = true;
        String message="Token is valid";
        try {
            verifyToken(request.getToken(), "access");
        } catch (AppException e) {
            isValid = false;
            message=e.getMessage();
        }
        return IntrospectResponse.builder()
            .valid(isValid)
            .message(message)
            .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userReponsitory.findByUsername(request.getUsername()).orElseThrow(
            () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
    
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        return AuthenticationResponse.builder()
            .accessToken(jwtTokenProvider.generateAccessToken(user))
            .refreshToken(jwtTokenProvider.generateRefreshToken(user))
            .authenticated(true)
            .build();
    }
    private SignedJWT verifyToken(String token,String type) throws ParseException, JOSEException {
        
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Object tokenTypeClaim = signedJWT.getJWTClaimsSet().getClaim("token-type");
        if (type.equals("refresh") && (tokenTypeClaim == null || !"refresh".equals(tokenTypeClaim.toString()))) {
            throw new AppException(ErrorCode.NOT_REFRESH_TOKEN);
        }
        
        Object scopeClaim = signedJWT.getJWTClaimsSet().getClaim("scope");
        if (type.equals("access") && (scopeClaim == null || scopeClaim.toString().isEmpty())) {
            throw new AppException(ErrorCode.NOT_ACCESS_TOKEN);
        }

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean isVerified = signedJWT.verify(verifier);
        if(!isVerified || !expirationTime.after(new Date())){
            System.out.println("Token is invalid");
            System.out.println("isVerified: "+isVerified);
            System.out.println("expirationTime: "+expirationTime);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        return signedJWT;
    }
    public void logout(LogoutRequest request) throws JOSEException, ParseException {
        var signedJWT=verifyToken(request.getToken(),"access");
        String id=signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidToken invalidToken = InvalidToken.builder()
            .tokenID(id)
            .expiryTime(expiryTime)
            .build();
        invalidatedRepository.save(invalidToken);
    }
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException{
        // verify the refresh token and invalidate it
        var signedJWT=verifyToken(request.getRefreshToken(),"refresh");
        String id=signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidToken invalidRefreshToken = InvalidToken.builder()
            .tokenID(id)
            .expiryTime(expiryTime)
            .build();
        invalidatedRepository.save(invalidRefreshToken);
        // generate a new access token
        User user= userReponsitory.findById(signedJWT.getJWTClaimsSet().getClaim("userId").toString()).orElseThrow(
            () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
        
        return AuthenticationResponse.builder()
            .accessToken(jwtTokenProvider.generateAccessToken(user))
            .refreshToken(jwtTokenProvider.generateRefreshToken(user))
            .authenticated(true)
            .build();

    }

}
