package com.minhyung.schedule.group.repository;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.group.domain.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    @Query("""
        select u
        from GroupMemberEntity gm
        join gm.user u
        where gm.group.id = :groupId
            and u.id = :userId
            and u.deletedAt is null
    """)
    Optional<UserEntity> findInviterCandidate(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
