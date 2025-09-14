package com.minhyung.schedule.group.controller.docs;

import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.group.dto.CreateGroupRequest;
import com.minhyung.schedule.group.dto.CreateGroupResponse;
import com.minhyung.schedule.security.principal.UserPrincipal;
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

@Tag(name = "Group", description = "일정 공유 그룹 생성, 관리, 삭제 API")
public interface ScheduleGroupApiDocs {
    @Operation(summary = "그룹 생성", description = "인증된 사용자로부터 생성할 그룹명을 입력받아 그룹을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "그룹 생성 완료",
                    headers = {
                            @Header(name = "Location", description = "생성된 리소스 URI", schema = @Schema(type = "string", example = "/api/v1/groups/1"))
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schemaProperties = {
                                    @SchemaProperty(name= "status", schema = @Schema(type = "integer", example = "201")),
                                    @SchemaProperty(name= "message", schema = @Schema(type = "string", example = "그룹 생성이 완료되었습니다.")),
                                    @SchemaProperty(name= "data", schema = @Schema(type = "object", example = """
                                            {
                                                "id": 1,
                                                "ownerId": 1,
                                                "groupName": "newGroup"
                                            }
                                            """))
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "VALIDATION_001", content = @Content),
            @ApiResponse(responseCode = "401", description = "AUTH_006", content = @Content),
            @ApiResponse(responseCode = "500", description = "SERVER_001", content = @Content)
    })
    ResponseEntity<ApiResult<CreateGroupResponse>> create(UserPrincipal principal, CreateGroupRequest request);
}
