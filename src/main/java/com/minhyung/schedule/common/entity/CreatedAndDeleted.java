package com.minhyung.schedule.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class CreatedAndDeleted extends CreatedOnly{
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public final void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
