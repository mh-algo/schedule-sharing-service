package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.auth.dto.UserInfoDto;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.repository.UserRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void 사용자_정보_조회() {
        // given
        String username = "username";
        UserInfoDto userInfoDto = new UserInfoDto(1L, username, "password123!", UserStatus.ACTIVE);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userInfoDto));

        // when
        UserInfoDto result = userService.getUserInfo(username);

        // then
        assertThat(result).isEqualTo(userInfoDto);
    }

    @Test
    void 사용자_정보_조회할_때_사용자_정보가_없는_경우() {
        // given
        String username = "username";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> userService.getUserInfo(username);

        // then
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(action)
                .withMessage("User not found: " + username);
    }
}