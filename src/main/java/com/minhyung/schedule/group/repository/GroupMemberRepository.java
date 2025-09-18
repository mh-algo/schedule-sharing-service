package com.minhyung.schedule.group.repository;

import com.minhyung.schedule.group.domain.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
}
