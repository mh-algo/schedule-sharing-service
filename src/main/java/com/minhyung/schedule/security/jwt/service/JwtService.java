package com.minhyung.schedule.security.jwt.service;

import com.minhyung.schedule.auth.dto.UserStatusDto;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.dto.IssuedToken;
import com.minhyung.schedule.security.jwt.dto.TokenData;
import com.minhyung.schedule.security.jwt.repository.TokenBlackList;
import com.minhyung.schedule.security.jwt.repository.TokenStore;
import com.minhyung.schedule.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class JwtService {
    private final JwtProvider jwtProvider;
    private final TokenStore tokenStore;
    private final TokenBlackList tokenBlackList;
    private final Clock clock;
    private final UserService userService;

    public JwtService(JwtProvider jwtProvider, TokenStore tokenStore, TokenBlackList tokenBlackList, Clock clock, UserService userService) {
        this.jwtProvider = jwtProvider;
        this.tokenStore = tokenStore;
        this.tokenBlackList = tokenBlackList;
        this.clock = clock;
        this.userService = userService;
    }

    public JwtToken issueJwtToken(UserPrincipal principal) {
        Instant now = Instant.now(clock);
        IssuedToken access = jwtProvider.issueAccess(principal, now);
        IssuedToken refresh = jwtProvider.issueRefresh(principal, now);
        tokenStore.save(refresh.sub(), refresh.token(), refresh.expiresAt());      // 생성된 refreshToken 저장
        return JwtToken.ofRaw(access.token(), refresh.token());
    }

    public JwtToken reissueJwtToken(String refreshToken) {
        Claims claims = verifyRefreshToken(refreshToken);       // refreshToken 검증
        String sub = claims.getSubject();
        saveBlackList(sub, refreshToken, "reissue");        // 기존 refreshToken 블랙리스트 등록
        UserStatusDto userStatus = userService.getUserStatusDto(Long.valueOf(sub));     // 사용자 정보 조회
        if (userStatus.status().isSuspended()) {    // 계정이 정지된 경우
            throw new DisabledException("Account is disabled");
        }
        return issueJwtToken(toPrincipal(userStatus));
    }

    private void saveBlackList(String sub, String refreshToken, String reason) throws JwtException {
        TokenData tokenData = tokenStore.find(sub).orElseThrow(() -> new JwtException("Invalid JWT"));
        tokenStore.remove(sub);
        tokenBlackList.save(refreshToken, reason, tokenData.expiresAt());
    }

    public Claims verifyAccessToken(String accessToken) throws IllegalArgumentException, JwtException {
        return jwtProvider.parseClaims(accessToken);
    }

    public Claims verifyRefreshToken(String refreshToken) throws IllegalArgumentException, JwtException {
        Claims claims = jwtProvider.parseClaims(refreshToken);
        if (tokenStore.isInvalid(claims.getSubject(), refreshToken) || tokenBlackList.isBlackListed(refreshToken)) {
            throw new JwtException("Invalid JWT");
        }
        return claims;
    }

    public UserPrincipal toPrincipal(UserStatusDto userStatus) {
        Long id = userStatus.id();
        boolean verified = !userStatus.status().isUnverified();
        return new UserPrincipal(id, verified);
    }
}
