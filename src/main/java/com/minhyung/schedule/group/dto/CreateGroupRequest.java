package com.minhyung.schedule.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateGroupRequest(
        @Schema(description = "생성할 그룹명", example = "newGroup", maximum = "50")
        @NotBlank(message = "그룹명을 입력해주세요.")
        @Length(min = 1, max = 50, message = "그룹명은 1자 이상 50자 이하로 입력해주세요.")
        String name
) {
}
