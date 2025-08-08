package com.minhyung.schedule.auth.repository;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(value = "select 1 from users where username = :username and deleted_at is null", nativeQuery = true)
    Optional<Integer> existsByUsername(@Param("username") String username);
}
