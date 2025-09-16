package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.dto.UserLoginDto;
import com.minhyung.schedule.auth.dto.UserStatusDto;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserLoginDto getUserLogin(String username) {
        return userRepository.findUserLoginByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public UserStatusDto getUserStatusDto(Long id) {
        return userRepository.findUserStatusById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
}
