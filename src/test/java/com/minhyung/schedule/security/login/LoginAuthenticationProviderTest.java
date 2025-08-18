package com.minhyung.schedule.security.login;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestLoginAuthenticationToken;
import com.minhyung.schedule.security.testsupport.TestLoginUserDetailsBuilder;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationProviderTest {
    @InjectMocks
    private LoginAuthenticationProvider loginAuthenticationProvider;

    @Mock
    private LoginUserDetailsService loginUserDetailsService;

    private static UsernamePasswordAuthenticationToken createUnauthenticated(String username, String password) {
        return TestLoginAuthenticationToken.unauthenticated(username, password);
    }

    @Test
    void 인증_성공() {
        // given
        String username = "username";
        String password = "password123!";
        UsernamePasswordAuthenticationToken unauthenticated = createUnauthenticated(username, password);
        LoginUserDetails userDetails = TestLoginUserDetailsBuilder.userDetails().build();

        when(loginUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // when
        Authentication authenticate = loginAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThat(authenticate).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authenticate;
        assertThat(authenticationToken.getPrincipal()).isEqualTo(toUserPrincipal(userDetails));
        assertThat(authenticationToken.getCredentials()).isEqualTo(password);
    }

    private UserPrincipal toUserPrincipal(LoginUserDetails userDetails) {
        LoginUserInfo account = userDetails.getUserInfo();
        boolean verified = !userDetails.isUnverified();
        return new UserPrincipal(account.id(), verified);
    }

    @ParameterizedTest
    @CsvSource(value = {
            // 아이디
            "'',password123!", // blank
            "한글입력,password123!", // 한글
            "!@#$,password123!",     // 특수문자
            "abc,password123!",  // 3글자
            "1234567890abc,password123!",    // 13글자

            // 비밀번호
            "username,''",  // blank
            "username,abc123!",  // 7글자
            "username,1234567890abcefghijk!",    // 21글자
            "username,한글1ab!",   // 한글
            "username,abcdefgh",     // 영문
            "username,12345678",     // 숫자
            "username,!@#$%^&*",     // 특수문자
            "username,abcd1234",     // 영문+숫자
            "username,abcd!@#$",     // 영문+특수문자
            "username,1234!@#$"     // 숫자+특수문자
    })
    void 아이디_비밀번호_패턴_불일치(String username, String password) {
        // given
        UsernamePasswordAuthenticationToken unauthenticated = createUnauthenticated(username, password);

        // when
        ThrowingCallable action = () -> loginAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThatExceptionOfType(BadCredentialsException.class)
                .isThrownBy(action);
    }

    @Test
    void 계정이_정지된_경우() {
        // given
        String username = "username";
        String password = "password123!";
        UsernamePasswordAuthenticationToken unauthenticated = createUnauthenticated(username, password);
        LoginUserDetails userDetails = TestLoginUserDetailsBuilder.userDetails()
                .withStatus(UserStatus.SUSPENDED)
                .build();

        when(loginUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // when
        ThrowingCallable action = () -> loginAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThatExceptionOfType(DisabledException.class)
                .isThrownBy(action);
    }

    @Test
    void loginUserDetailsService의_loadUserByUsername_메서드가_LoginUserDetails를_반환하지_않을_경우() {
        // given
        String username = "username";
        String password = "password123!";
        UsernamePasswordAuthenticationToken unauthenticated = createUnauthenticated(username, password);

        when(loginUserDetailsService.loadUserByUsername(username))
                .thenReturn(User.builder()
                        .username(username)
                        .password("{bcrypt}$2a$10$9tJM5zUrYimpTepZ5WraUuCvVxZZXMc2M4J92fTqMaECAnZRNkRGa")
                        .build()
                );

        // when
        ThrowingCallable action = () -> loginAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThatExceptionOfType(InternalAuthenticationServiceException.class)
                .isThrownBy(action)
                .withMessage("Unexpected principal type");
    }
}