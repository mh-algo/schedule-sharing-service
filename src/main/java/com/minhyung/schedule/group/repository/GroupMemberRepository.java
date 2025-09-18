package com.minhyung.schedule.group.repository;

import com.minhyung.schedule.group.domain.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    @Query(value = "select 1 from group_members where user_id = :userId and deleted_at is null", nativeQuery = true)
    Optional<Integer> existsByUserId(@Param("userId") Long userId);
}
