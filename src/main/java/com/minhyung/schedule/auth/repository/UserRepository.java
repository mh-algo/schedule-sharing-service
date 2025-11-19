package com.minhyung.schedule.auth.repository;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.dto.UserLoginDto;
import com.minhyung.schedule.auth.dto.UserStatusDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(value = "select 1 from users where username = :username and deleted_at is null", nativeQuery = true)
    Optional<Integer> existsByUsername(@Param("username") String username);

    @Query("""
        select new com.minhyung.schedule.auth.dto.UserLoginDto(u.id, u.username, u.password, u.status)
        from UserEntity u
        where u.username = :username and u.deletedAt is null
    """)
    Optional<UserLoginDto> findUserLoginByUsername(@Param("username") String username);

    @Query("""
        select new com.minhyung.schedule.auth.dto.UserStatusDto(u.id, u.status)
        from UserEntity u
        where u.id = :id and u.deletedAt is null
    """)
    Optional<UserStatusDto> findUserStatusById(@Param("id") Long id);

    Optional<UserEntity> findByUsername(String username);

    // 초대 받는 사람이 그룹 소속이 아닌지 검증
    // 초대 받는 사람이 그룹 초대 상태인지 확인(초대가 만료되지 않았고, 응답하지 않은 경우 초대 x)
    @Query("""
        select u
        from UserEntity u
        where u.username = :username
            and u.deletedAt is null
            and not exists (
                select 1
                from GroupMemberEntity gm
                where gm.user = u
                    and gm.group.id = :groupId
            )
            and not exists (
                select 1
                from GroupInviteEntity gi
                where gi.group.id = :groupId
                    and gi.invitee = u
                    and gi.respondedAt is null
                    and gi.expiresAt > CURRENT_TIMESTAMP
            )
    """)
    Optional<UserEntity> findInvitableUser(@Param("username") String username,
                                           @Param("groupId") Long groupId);
}
