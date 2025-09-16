package com.minhyung.schedule.security.login;

import com.minhyung.schedule.auth.dto.UserLoginDto;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import com.minhyung.schedule.security.testsupport.TestUserLoginDtoBuilder;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserDetailsServiceTest {
    @InjectMocks
    private LoginUserDetailsService loginUserDetailsService;

    @Mock
    private UserService userService;

    @Test
    void 사용자_정보_조회_후_UserDetails_반환() {
        // given
        String username = "username";
        UserLoginDto userLoginDto = TestUserLoginDtoBuilder.user().build();

        when(userService.getUserLogin(username)).thenReturn(userLoginDto);

        // when
        UserDetails result = loginUserDetailsService.loadUserByUsername(username);

        // then
        assertThat(result).isInstanceOf(LoginUserDetails.class);
        LoginUserDetails userDetails = (LoginUserDetails) result;
        LoginUserInfo loginUserInfo = userDetails.getUserInfo();
        assertThat(loginUserInfo.id()).isEqualTo(userLoginDto.id());
        assertThat(loginUserInfo.username()).isEqualTo(userLoginDto.username());
        assertThat(loginUserInfo.password()).isEqualTo(userLoginDto.password());
        assertThat(loginUserInfo.status()).isEqualTo(userLoginDto.status());
    }

    @Test
    void 사용자_정보_조회_실패() {
        // given
        String username = "username";

        when(userService.getUserLogin(username)).thenThrow(new UserNotFoundException("User not found: " + username));

        // when
        ThrowingCallable action = () -> loginUserDetailsService.loadUserByUsername(username);

        // when
        assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(action);
    }
}