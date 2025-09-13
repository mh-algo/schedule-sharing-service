package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {
    private final JwtService jwtService;

    public void logout(String refreshHeader) throws ApiException {
        try {
            JwtToken token = JwtToken.ofBearer(null, refreshHeader);
            String refreshToken = token.getRefreshToken();       // prefix 제거
            jwtService.invalidateRefreshToken(refreshToken, "logout");   // refresh Token 무효화
        } catch (IllegalArgumentException | JwtException e) {
            throw new ApiException(AuthenticationErrorCode.INVALID_AUTHENTICATION);
        }
    }
}
