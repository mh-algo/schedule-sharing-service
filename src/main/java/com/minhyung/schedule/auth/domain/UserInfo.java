package com.minhyung.schedule.auth.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfo {
    private final String username;
    private final String password;      // 암호화된 password

    public static UserInfo fromRaw(String username, String rawPassword, PasswordEncoder encoder) {
        return new UserInfo(username, encoder.encode(rawPassword));
    }

    public static UserInfo fromEncrypted(String username, String encryptedPassword) {
        return new UserInfo(username, encryptedPassword);
    }
}
