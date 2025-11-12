package com.minhyung.schedule.notification.controller.docs;

import com.minhyung.schedule.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE", description = "SSE 연결 요청 API")
public interface SseApiDocs {
    @Operation(summary = "SSE 연결 요청", description = "인증된 사용자와 서버 간에 SSE(Server-Sent Events) 연결을 수립하여 실시간 알림을 수신할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 생성 완료",
                    headers = {
                            @Header(name = "Last-Event-ID", description = "마지막으로 수신한 이벤트 ID (재연결 시 사용)", schema = @Schema(type = "integer", example = "12"))
                    },
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            examples = @ExampleObject(value = """
                                            id:1761552161387
                                            event:heartbeat
                                            data:connected
                                            
                                            id:12
                                            event:message
                                            data:{"type":"INVITE_CREATED","data":{"inviteId":12,"groupId":1,"groupName":"testGroup","inviter":{"id":1,"username":"username"},"createdAt":[2025,10,27,18,50,56,524626200],"expiresAt":[2025,11,3,18,50,56,524626200]}}
                                            
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "400", content = @Content)
    })
    ResponseEntity<SseEmitter> connect(UserPrincipal principal, Long lastEventId);
}
