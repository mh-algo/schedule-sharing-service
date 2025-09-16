package com.minhyung.schedule.group.controller;

import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.group.controller.docs.ScheduleGroupApiDocs;
import com.minhyung.schedule.group.dto.CreateGroupRequest;
import com.minhyung.schedule.group.dto.CreateGroupResponse;
import com.minhyung.schedule.group.dto.InviteUserRequest;
import com.minhyung.schedule.group.service.ScheduleGroupService;
import com.minhyung.schedule.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(ApiPaths.GROUP)
@RequiredArgsConstructor
public class ScheduleGroupController implements ScheduleGroupApiDocs {
    private final ScheduleGroupService scheduleGroupService;

    @PostMapping
    public ResponseEntity<ApiResult<CreateGroupResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @RequestBody @Valid CreateGroupRequest request) {
        CreateGroupResponse response = scheduleGroupService.create(principal.id(), request);
        return ResponseEntity.created(URI.create(ApiPathsUtils.group(response.id())))
                .body(ApiResult.created("그룹 생성이 완료되었습니다.", response));
    }

    @PostMapping("{groupId}/invite")
    public ResponseEntity<ApiResult<Void>> invite(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long groupId, @RequestBody @Valid InviteUserRequest request) {
        scheduleGroupService.invite(principal.id(), groupId, request);
        return ResponseEntity.ok().body(ApiResult.success());
    }
}
