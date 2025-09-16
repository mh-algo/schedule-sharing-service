package com.minhyung.schedule.group.repository;

import com.minhyung.schedule.group.domain.entity.GroupInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteRepository extends JpaRepository<GroupInviteEntity, Long> {
}
