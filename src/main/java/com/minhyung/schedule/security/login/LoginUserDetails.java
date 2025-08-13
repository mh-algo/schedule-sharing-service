package com.minhyung.schedule.security.login;

import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class LoginUserDetails implements UserDetails {
    private final LoginUserInfo userInfo;
    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(LoginUserInfo userInfo, List<GrantedAuthority> authorities) {
        this.userInfo = userInfo;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public LoginUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public String getPassword() {
        return userInfo.password();
    }

    @Override
    public String getUsername() {
        return userInfo.username();
    }

    @Override
    public boolean isEnabled() {
        return userInfo.status().isActive();
    }

    public boolean isSuspended() {
        return userInfo.status().isSuspended();
    }

    public boolean isUnverified() {
        return userInfo.status().isUnverified();
    }

}
