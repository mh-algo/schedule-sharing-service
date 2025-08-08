package com.minhyung.schedule.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.auth.dto.SignupRequest;
import com.minhyung.schedule.auth.dto.SignupResponse;
import com.minhyung.schedule.auth.service.SignupService;
import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.common.exception.ApiExceptionHandler;
import com.minhyung.schedule.common.exception.ValidationErrorCode;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private SignupService signupService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(signupService))
                .setValidator(new LocalValidatorFactoryBean())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private static final String SIGNUP_PATH = ApiPaths.AUTH + "/signup";
    private ObjectMapper objectMapper = TestObjectMapper.getInstance();

    private static SignupRequest createSignupRequest(String username, String password) {
        return new SignupRequest(username, password, password);
    }

    @Test
    void 회원가입_성공() throws Exception {
        // given
        Long id = 1L;
        String username = "test";
        String password = "test123!";
        SignupRequest request = createSignupRequest(username, password);
        SignupResponse response = new SignupResponse(id, username);
        String content = objectMapper.writeValueAsString(request);

        // when
        when(signupService.signup(request)).thenReturn(response);
        ResultActions resultActions = mockMvc.perform(post(SIGNUP_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.MEMBER + "/" + response.id()))
                .andExpect(jsonPath("status").value(201))
                .andExpect(jsonPath("message").value("회원가입이 완료되었습니다."));
    }

    @ParameterizedTest
    @NullSource
    void 아이디가_null일_경우(String username) throws Exception {
        // given
        String password = "test123!";
        SignupRequest request = createSignupRequest(username, password);
        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(SIGNUP_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        ValidationErrorCode errorCode = ValidationErrorCode.VALIDATION_EXCEPTION;
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("code").value(errorCode.getCode()))
                .andExpect(jsonPath("message").value("username: 아이디를 입력해주세요."));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {
            "한글입력", // 한글
            "!@#$",     // 특수문자
            "abc",  // 3글자
            "1234567890abc"    // 13글자
    })
    void 아이디_패턴_불일치(String username) throws Exception {
        // given
        String password = "test123!";
        SignupRequest request = createSignupRequest(username, password);
        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(SIGNUP_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        ValidationErrorCode errorCode = ValidationErrorCode.VALIDATION_EXCEPTION;
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("code").value(errorCode.getCode()))
                .andExpect(jsonPath("message").value("username: 아이디는 4~12자리 영문, 숫자만 가능합니다."));
    }

    @ParameterizedTest
    @NullSource
    void 비밀번호가_null일_경우(String password) throws Exception {
        // given
        String username = "test";
        SignupRequest request = createSignupRequest(username, password);
        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(SIGNUP_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        ValidationErrorCode errorCode = ValidationErrorCode.VALIDATION_EXCEPTION;
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("code").value(errorCode.getCode()))
                .andExpect(jsonPath("message").value("password: 비밀번호를 입력해주세요."));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {
            "abc123!",  // 7글자
            "1234567890abcefghijk!",    // 21글자
            "한글1ab!",   // 한글
            "abcdefgh",     // 영문
            "12345678",     // 숫자
            "!@#$%^&*",     // 특수문자
            "abcd1234",     // 영문+숫자
            "abcd!@#$",     // 영문+특수문자
            "1234!@#$"     // 숫자+특수문자
    })
    void 비밀번호_패턴_불일치(String password) throws Exception {
        // given
        String username = "test";
        SignupRequest request = createSignupRequest(username, password);
        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(SIGNUP_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        ValidationErrorCode errorCode = ValidationErrorCode.VALIDATION_EXCEPTION;
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("code").value(errorCode.getCode()))
                .andExpect(jsonPath("message").value("password: 비밀번호는 8~20자 이상으로 영문, 숫자, 특수문자를 각각 1가지 이상 사용하여 조합해주세요."));
    }

    @Test
    void 비밀번호와_비밀번호_확인_불일치() throws Exception {
        // given
        String username = "test";
        String password = "test123!";
        String passwordConfirm = "test456!";
        SignupRequest request = new SignupRequest(username, password, passwordConfirm);
        String content = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(SIGNUP_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        ValidationErrorCode errorCode = ValidationErrorCode.VALIDATION_EXCEPTION;
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("code").value(errorCode.getCode()))
                .andExpect(jsonPath("message").value("비밀번호가 일치하지 않습니다."));
    }
}