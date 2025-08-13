package com.minhyung.schedule.security.login;

import com.minhyung.schedule.auth.dto.UserInfoDto;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class LoginUserDetailsService implements UserDetailsService {
    private static final GrantedAuthority DEFAULT_ROLE = new SimpleGrantedAuthority("ROLE_USER");
    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES = List.of(DEFAULT_ROLE);
    private final UserService userService;

    public LoginUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserInfoDto userInfo = userService.getUserInfo(username);
            return new LoginUserDetails(toLoginUserInfo(userInfo), DEFAULT_AUTHORITIES);
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    private static LoginUserInfo toLoginUserInfo(UserInfoDto userInfo) {
        return LoginUserInfo.builder()
                .id(userInfo.id())
                .username(userInfo.username())
                .password(userInfo.password())
                .status(userInfo.status())
                .build();
    }
}
