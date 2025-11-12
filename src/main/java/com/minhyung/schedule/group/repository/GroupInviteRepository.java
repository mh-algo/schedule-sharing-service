package com.minhyung.schedule.group.repository;

import com.minhyung.schedule.group.domain.entity.GroupInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInviteEntity, Long> {
    @Query(value = """
        select 1
        from group_invites
        where group_id = :groupId and inviter_id = :inviterId and invitee_id = :inviteeId
          and responded_at is null and expires_at > CURRENT_TIMESTAMP;
    """, nativeQuery = true)
    Optional<Long> existsValidGroupInvite(@Param("groupId") Long groupId,
                                          @Param("inviterId") Long inviterId,
                                          @Param("inviteeId") Long inviteeId);
}
