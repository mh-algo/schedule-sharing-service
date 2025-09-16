package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.auth.dto.UserLoginDto;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.repository.UserRepository;
import com.minhyung.schedule.security.testsupport.TestUserLoginDtoBuilder;
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
        UserLoginDto userLoginDto = TestUserLoginDtoBuilder.user().build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userLoginDto));

        // when
        UserLoginDto result = userService.getUserLogin(username);

        // then
        assertThat(result).isEqualTo(userLoginDto);
    }

    @Test
    void 사용자_정보_조회할_때_사용자_정보가_없는_경우() {
        // given
        String username = "username";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> userService.getUserLogin(username);

        // then
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(action)
                .withMessage("User not found: " + username);
    }
}