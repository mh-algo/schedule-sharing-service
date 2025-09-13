package com.minhyung.schedule.security.auth;

import com.minhyung.schedule.security.jwt.JwtToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private JwtToken token;

    public JwtAuthenticationToken(Object principal, JwtToken token) {
        super((Collection) null);
        this.principal = principal;
        this.token = token;
        this.setAuthenticated(false);
    }

    public JwtAuthenticationToken(Object principal, JwtToken token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        super.setAuthenticated(true);
    }

    public static JwtAuthenticationToken unauthenticated(JwtToken token) {
        return new JwtAuthenticationToken(null, token);
    }

    public static JwtAuthenticationToken authenticated(Object principal, JwtToken token, Collection<? extends GrantedAuthority> authorities) {
        return new JwtAuthenticationToken(principal, token, authorities);
    }

    public JwtToken getToken() {
        return this.token;
    }

    @Override
    public Object getCredentials() {
        return this.getToken();
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public void eraseToken() {
        this.token = null;
    }
}
