package com.minhyung.schedule.auth.controller;

import com.minhyung.schedule.auth.controller.docs.AuthApiDocs;
import com.minhyung.schedule.auth.dto.SignupRequest;
import com.minhyung.schedule.auth.dto.SignupResponse;
import com.minhyung.schedule.auth.service.LogoutService;
import com.minhyung.schedule.auth.service.SignupService;
import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.security.jwt.JwtHeader;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
public class AuthController implements AuthApiDocs {
    private final SignupService signupService;
    private final LogoutService logoutService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResult<SignupResponse>> signup(@RequestBody @Valid SignupRequest request) {
        SignupResponse response = signupService.signup(request);
        return ResponseEntity.created(URI.create(ApiPathsUtils.auth(response.id())))
                .body(ApiResult.created("회원가입이 완료되었습니다.", response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@RequestHeader(JwtHeader.REFRESH_TOKEN) String refreshHeader) {
        logoutService.logout(refreshHeader);
        return ResponseEntity.ok(ApiResult.success("로그아웃 되었습니다."));
    }
}
