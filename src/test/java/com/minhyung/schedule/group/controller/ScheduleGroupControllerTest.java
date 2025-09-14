package com.minhyung.schedule.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.AuthenticatedTest;
import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.common.exception.ApiExceptionHandler;
import com.minhyung.schedule.common.exception.ValidationErrorCode;
import com.minhyung.schedule.group.dto.CreateGroupRequest;
import com.minhyung.schedule.group.dto.CreateGroupResponse;
import com.minhyung.schedule.group.service.ScheduleGroupService;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class ScheduleGroupControllerTest extends AuthenticatedTest {
    @Mock
    private ScheduleGroupService scheduleGroupService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ScheduleGroupController(scheduleGroupService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(getAuthentication());
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static final String CREATE_PATH = ApiPaths.GROUP;
    private static final ObjectMapper objectMapper = TestObjectMapper.getInstance();

    @ParameterizedTest
    @ValueSource(strings = {
            "newGroup",
            "a",    // 1글자
            "12345678901234567890123456789012345678901234567890"    // 50글자
    })
    void 그룹_생성_성공(String groupName) throws Exception {
        // given
        Long id = 1L;
        Long ownerId = 1L;
        CreateGroupRequest request = new CreateGroupRequest(groupName);
        CreateGroupResponse response = new CreateGroupResponse(id, ownerId, groupName);
        String content = objectMapper.writeValueAsString(request);

        when(scheduleGroupService.create(id, request)).thenReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post(CREATE_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPathsUtils.group(response.id())))
                .andExpect(jsonPath("status").value(201))
                .andExpect(jsonPath("message").value("그룹 생성이 완료되었습니다."))
                .andExpect(jsonPath("data.id").value(response.id()))
                .andExpect(jsonPath("data.ownerId").value(response.ownerId()))
                .andExpect(jsonPath("data.groupName").value(response.groupName()));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = {
            "123456789012345678901234567890123456789012345678901",    // 51글자
            "        "          // blank
    })
    void 유효하지_않는_그룹명(String groupName) throws Exception {
        // given
        CreateGroupRequest request = new CreateGroupRequest(groupName);
        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(CREATE_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        ValidationErrorCode errorCode = ValidationErrorCode.VALIDATION_EXCEPTION;
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("code").value(errorCode.getCode()));
    }
}