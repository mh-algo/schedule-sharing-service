package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.dto.SignupRequest;
import com.minhyung.schedule.auth.dto.SignupResponse;
import com.minhyung.schedule.auth.exception.SignupErrorCode;
import com.minhyung.schedule.auth.repository.UserRepository;
import com.minhyung.schedule.common.exception.ApiException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {
    @InjectMocks
    private SignupService signupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입_성공() {
        // given
        Long id = 1L;
        String username = "test";
        String password = "test123!";
        SignupRequest request = new SignupRequest(username, password, password);
        SignupResponse response = new SignupResponse(id, username);
        UserEntity userEntity = createUserEntity(id, username, password);

        when(userRepository.existsByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // when
        SignupResponse result = signupService.signup(request);

        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void 아이디_중복() {
        // given
        Long id = 1L;
        String username = "test";
        String password = "test123!";
        SignupRequest request = new SignupRequest(username, password, password);

        when(userRepository.existsByUsername(username)).thenReturn(Optional.of(1));

        // when
        ThrowingCallable action = () -> signupService.signup(request);

        // then
        SignupErrorCode errorCode = SignupErrorCode.DUPLICATED_USERNAME;

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(action)
                .withMessage(errorCode.getMessage());
    }

    private static UserEntity createUserEntity(Long id, String username, String password) {
        UserEntity userEntity = new UserEntity(username, password);
        ReflectionTestUtils.setField(userEntity, "id", id);
        return userEntity;
    }
}