package com.minhyung.schedule.auth.repository;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.dto.UserInfoDto;
import com.minhyung.schedule.auth.dto.UserStatusDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(value = "select 1 from users where username = :username and deleted_at is null", nativeQuery = true)
    Optional<Integer> existsByUsername(@Param("username") String username);

    @Query("""
        select new com.minhyung.schedule.auth.dto.UserInfoDto(u.id, u.username, u.password, u.status)
        from UserEntity u
        where u.username = :username and u.deletedAt is null
    """)
    Optional<UserInfoDto> findByUsername(@Param("username") String username);

    @Query("""
        select new com.minhyung.schedule.auth.dto.UserStatusDto(u.id, u.status)
        from UserEntity u
        where u.id = :id and u.deletedAt is null
    """)
    Optional<UserStatusDto> findByUserId(@Param("id") Long id);
}
