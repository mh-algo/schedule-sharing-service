package com.minhyung.schedule.security.login;

import com.minhyung.schedule.common.PasswordRules;
import com.minhyung.schedule.common.UsernameRules;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import com.minhyung.schedule.security.principal.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;

public class LoginAuthenticationProvider extends DaoAuthenticationProvider {
    public LoginAuthenticationProvider(UserDetailsService userDetailsService) {
        super(userDetailsService);
        setPreAuthenticationChecks((user) -> {});   // 아이디, 비밀번호 검증 전에 수행할 검증 설정
        setPostAuthenticationChecks(new DefaultPostAuthenticationChecks());     // 아이디, 비밀번호 검증 후에 수행할 검증 설정
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        validateLoginPattern(authentication.getName(), authentication.getCredentials().toString());   // DB 조회 전 패턴 검증
        return super.authenticate(authentication);  // DB 조회 및 검증
    }

    private void validateLoginPattern(String username, String password) {
        if (!username.matches(UsernameRules.REGEX) || !password.matches(PasswordRules.REGEX)) {
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        if (principal instanceof LoginUserDetails userDetails) {
            UserPrincipal userPrincipal = toUserPrincipal(userDetails);     // userDetails에서 필요한 데이터만 principal로 등록
            return super.createSuccessAuthentication(userPrincipal, authentication, user);  // AuthenticationToken 생성 후 반환
        } else {
            throw new InternalAuthenticationServiceException("Unexpected principal type");
        }
    }

    private UserPrincipal toUserPrincipal(LoginUserDetails userDetails) {
        LoginUserInfo account = userDetails.getUserInfo();
        boolean verified = !userDetails.isUnverified();
        return new UserPrincipal(account.id(), verified);
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
        private DefaultPostAuthenticationChecks() {
        }

        public void check(UserDetails user) {
            if (!user.isEnabled()) {
                LoginAuthenticationProvider.super.logger.debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(LoginAuthenticationProvider.super.messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account is disabled"));
            }
        }
    }
}
