package com.minhyung.schedule.group.dto;

import com.minhyung.schedule.common.UsernameRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record InviteUserRequest(
        @Schema(description = "초대할 사용자명", example = "username2", requiredMode = Schema.RequiredMode.REQUIRED)
        @Pattern(regexp = UsernameRules.REGEX, message = "아이디는 4~12자리 영문, 숫자만 가능합니다.")
        String username
) {
}
