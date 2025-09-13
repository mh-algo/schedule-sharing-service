package com.minhyung.schedule.auth;

import com.minhyung.schedule.AuthenticatedTest;
import com.minhyung.schedule.common.ApiPathsUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LogoutIntegrationTest extends AuthenticatedTest {
    private static final String REQUEST_PATH = ApiPathsUtils.auth("logout");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 로그아웃() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(post(REQUEST_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(getHttpHeaders())
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("로그아웃 되었습니다."));
    }
}
