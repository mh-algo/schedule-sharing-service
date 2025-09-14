package com.minhyung.schedule.group.repository;

import com.minhyung.schedule.group.domain.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
}
