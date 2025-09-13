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
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "회원 가입, 로그인, 로그아웃 등 인증 관련 API")
public interface AuthApiDocs {
    @Operation(summary = "회원가입", description = "사용자로부터 아이디와 비밀번호 등의 정보를 입력받아 회원으로 등록합니다.")
    @SecurityRequirements
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

    @Operation(summary = "로그인", description = "사용자로부터 아이디와 비밀번호를 입력받아 로그인합니다.")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 완료",
                    headers = {
                            @Header(name = "Authorization", description = "Access Token",
                                    schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiO...")),
                            @Header(name = "Refresh-Token", description = "Refresh Token",
                                    schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiO..."))
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schemaProperties = {
                                    @SchemaProperty(name= "status", schema = @Schema(type = "integer", example = "200")),
                                    @SchemaProperty(name= "message", schema = @Schema(type = "string", example = "로그인 성공"))
                            }
                    )
            ),
            @ApiResponse(responseCode = "401", description = "LOGIN_001", content = @Content),
            @ApiResponse(responseCode = "403", description = "LOGIN_002", content = @Content),
            @ApiResponse(responseCode = "500", description = "LOGIN_003", content = @Content),
            @ApiResponse(responseCode = "405", description = "AUTH_001", content = @Content),
            @ApiResponse(responseCode = "400", description = "AUTH_002", content = @Content),
            @ApiResponse(responseCode = "400", description = "AUTH_003", content = @Content)
    })
    @PostMapping("/login")
    default void login(@RequestBody LoginRequest request) {
    }

    record LoginRequest(
            @Schema(description = "아이디", example = "username", requiredMode = Schema.RequiredMode.REQUIRED)
            String username,
            @Schema(description = "비밀번호", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
            String password
    ) {
    }

    @Operation(summary = "로그아웃", description = "인증된 사용자의 토큰을 무효화하여 로그아웃합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 완료",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schemaProperties = {
                                    @SchemaProperty(name= "status", schema = @Schema(type = "integer", example = "200")),
                                    @SchemaProperty(name= "message", schema = @Schema(type = "string", example = "로그아웃 되었습니다."))
                            }
                    )
            ),
            @ApiResponse(responseCode = "405", description = "AUTH_001", content = @Content),
            @ApiResponse(responseCode = "401", description = "AUTH_004", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content)
    })
    ResponseEntity<ApiResult<Void>> logout(String refreshToken);
}
