package com.minhyung.schedule.security.principal;

import com.minhyung.schedule.auth.dto.UserStatusDto;
import com.minhyung.schedule.security.login.LoginUserDetails;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import io.jsonwebtoken.Claims;

public final class UserPrincipalMapper {
    public static UserPrincipal from(LoginUserDetails userDetails) {
        LoginUserInfo account = userDetails.getUserInfo();
        boolean verified = !userDetails.isUnverified();
        return new UserPrincipal(account.id(), verified);
    }

    public static UserPrincipal from(UserStatusDto userStatus) {
        Long id = userStatus.id();
        boolean verified = !userStatus.status().isUnverified();
        return new UserPrincipal(id, verified);
    }

    public static UserPrincipal from(Claims claims) {
        Long id = Long.valueOf(claims.getSubject());
        Boolean verified = claims.get("verified", Boolean.class);
        return new UserPrincipal(id, verified);
    }
}
