package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.auth.domain.UserInfo;
import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.dto.SignupRequest;
import com.minhyung.schedule.auth.dto.SignupResponse;
import com.minhyung.schedule.auth.exception.SignupErrorCode;
import com.minhyung.schedule.auth.repository.UserRepository;
import com.minhyung.schedule.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        // password 암호화
        UserInfo userInfo = UserInfo.fromRaw(request.username(), request.password(), passwordEncoder);

        validateUniqueUsername(userInfo.getUsername());     // username unique 검증
        UserEntity userEntity = UserEntity.of(userInfo.getUsername(), userInfo.getPassword());
        UserEntity savedUserEntity = userRepository.save(userEntity);
        return new SignupResponse(savedUserEntity.getId(), savedUserEntity.getUsername());
    }

    private void validateUniqueUsername(String username) {
        userRepository.existsByUsername(username)
                .ifPresent(exist -> {
                    throw new ApiException(SignupErrorCode.DUPLICATED_USERNAME);
                });
    }
}
