package com.minhyung.schedule.auth.controller;

import com.minhyung.schedule.auth.controller.docs.AuthApiDocs;
import com.minhyung.schedule.auth.dto.SignupRequest;
import com.minhyung.schedule.auth.dto.SignupResponse;
import com.minhyung.schedule.auth.service.SignupService;
import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.common.ApiResult;
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

    @PostMapping("/signup")
    public ResponseEntity<ApiResult<Void>> signup(@RequestBody @Valid SignupRequest request) {
        SignupResponse response = signupService.signup(request);
        return ResponseEntity.created(URI.create(ApiPaths.MEMBER + "/" + response.id()))
                .body(ApiResult.created("회원가입이 완료되었습니다."));
    }
}
