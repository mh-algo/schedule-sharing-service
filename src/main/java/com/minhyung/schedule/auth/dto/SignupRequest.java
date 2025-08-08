package com.minhyung.schedule.auth.dto;

import com.minhyung.schedule.auth.validator.FieldsMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@FieldsMatch(first = "password", second = "passwordConfirm", message = "비밀번호가 일치하지 않습니다.")
public record SignupRequest(
        @Schema(description = "가입할 아이디", example = "username")
        @NotNull(message = "아이디를 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자리 영문, 숫자만 가능합니다.")
        String username,

        @Schema(description = "비밀번호", example = "password123!")
        @NotNull(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
                message = "비밀번호는 8~20자 이상으로 영문, 숫자, 특수문자를 각각 1가지 이상 사용하여 조합해주세요.")
        String password,

        @Schema(description = "비밀번호 확인", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        String passwordConfirm
) {
}
