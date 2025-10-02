package com.minhyung.schedule.notification.controller;

import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.notification.service.SseService;
import com.minhyung.schedule.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping(ApiPaths.BASE_API)
@RequiredArgsConstructor
public class NotificationController {
    private final SseService sseService;

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@AuthenticationPrincipal UserPrincipal principal,
                              @RequestHeader(value="Last-Event-ID", required=false) Long lastEventId) {
        return sseService.connect(principal.id(), lastEventId);
    }
}
