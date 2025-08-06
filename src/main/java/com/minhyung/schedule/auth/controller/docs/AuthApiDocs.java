package com.minhyung.schedule.auth.controller.docs;

import com.minhyung.schedule.auth.dto.SignupRequest;
import com.minhyung.schedule.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "회원 가입, 로그인, 로그아웃 등 인증 관련 API")
public interface AuthApiDocs {
    @Operation(summary = "회원가입", description = "사용자로부터 아이디와 비밀번호 등의 정보를 입력받아 회원으로 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원 가입 완료",
                    headers = {
                            @Header(name = "Location", description = "생성된 리소스 URI", schema = @Schema(type = "string", example = "/api/v1/members/1"))
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schemaProperties = {
                                    @SchemaProperty(name= "status", schema = @Schema(type = "integer", example = "201")),
                                    @SchemaProperty(name= "message", schema = @Schema(type = "string", example = "회원가입이 완료되었습니다."))
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "VALIDATION_001", content = @Content),
            @ApiResponse(responseCode = "409", description = "SIGNUP_001", content = @Content),
            @ApiResponse(responseCode = "500", description = "SERVER_001", content = @Content)
    })
    ResponseEntity<ApiResult<Void>> signup(SignupRequest request);
}
