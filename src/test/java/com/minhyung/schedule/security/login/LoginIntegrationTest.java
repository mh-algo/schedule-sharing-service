package com.minhyung.schedule.security.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.JwtUtils;
import com.minhyung.schedule.security.jwt.dto.TokenData;
import com.minhyung.schedule.security.jwt.repository.InMemoryTokenStore;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginIntegrationTest {
    private static final String REQUEST_PATH = ApiPathsUtils.auth("login");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryTokenStore tokenStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    public String jwtSecretKey;

    @Test
    void 로그인_성공() throws Exception {
        // given
        String username = "username";
        String password = "password123!";
        String content = creatContent(username, password);

        // when
        ResultActions resultActions = mockMvc.perform(post(REQUEST_PATH)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );

        // then
        MvcResult result = resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("로그인 성공"))
                .andExpect(header().string(JwtHeader.ACCESS_TOKEN, startsWith(JwtToken.PREFIX)))
                .andExpect(header().string(JwtHeader.REFRESH_TOKEN, startsWith(JwtToken.PREFIX)))
                .andReturn();

        JwtToken jwtToken = getJwtToken(result.getResponse());
        String sub = "1";

        // accessToken 검증
        String accessToken = jwtToken.getAccessToken();
        Claims accessTokenClaims = JwtUtils.decode(accessToken, jwtSecretKey);
        assertThat(accessTokenClaims.getSubject()).isEqualTo(sub);
        assertThat(accessTokenClaims.get("verified", Boolean.class)).isEqualTo(true);

        // refreshToken 검증
        String refreshToken = jwtToken.getRefreshToken();
        Claims refreshTokenClaims = JwtUtils.decode(refreshToken, jwtSecretKey);
        assertThat(refreshTokenClaims.getSubject()).isEqualTo(sub);

        // refreshToken 저장 여부 검증
        Optional<TokenData> tokenData = tokenStore.find(sub);
        assertThat(tokenData).isNotEmpty();
        assertThat(tokenData.get().token()).isEqualTo(refreshToken);
    }

    private static JwtToken getJwtToken(MockHttpServletResponse response) {
        String authHeader = response.getHeader(JwtHeader.ACCESS_TOKEN);
        String refreshHeader = response.getHeader(JwtHeader.REFRESH_TOKEN);
        return JwtToken.ofBearer(authHeader, refreshHeader);
    }

    private String creatContent(String username, String password) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new LoginRequest(username, password));
    }

    record LoginRequest(
            String username,
            String password
    ) {
    }
}
