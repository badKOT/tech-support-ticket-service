package authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import self.project.web.ticket.service.Runner;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.entity.UserRole;
import self.project.web.ticket.service.repository.RefreshTokenRepository;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.service.PasswordService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Runner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        PasswordService.PasswordData passwordData = passwordService.encode("diana123");

        User diana = new User("diana", "Diana Prince", "diana@example.com",
            passwordData.passwordHash(), passwordData.salt(), UserRole.ADMIN);

        userRepository.saveAndFlush(diana);
    }

    @Test
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginWithCorrectUsernameAndPassword() throws Exception {

        String body = """
            {
              "username": "diana",
              "password": "diana123"
            }
            """;

        mockMvc.perform(
                post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.username").value("diana"))
            .andExpect(jsonPath("$.user.role").value("ADMIN"))
            .andExpect(jsonPath("$.user.enabled").value(true));
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {

        String body = """
            {
              "username": "diana",
              "password": "wrong-password"
            }
            """;

        mockMvc.perform(
                post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateWithJwt() throws Exception {

        JsonNode loginResponse = login();

        String accessToken = loginResponse.get("accessToken").asText();

        mockMvc.perform(
                get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("diana"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void shouldRefreshAccessToken() throws Exception {

        JsonNode loginResponse = login();

        String oldRefreshToken = loginResponse.get("refreshToken").asText();

        String refreshBody = """
            {
              "refreshToken": "%s"
            }
            """.formatted(oldRefreshToken);

        var refreshResult = mockMvc.perform(
                post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshBody))
            .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer")).andReturn();

        JsonNode refreshResponse = objectMapper.readTree(
            refreshResult.getResponse().getContentAsString());

        String newAccessToken = refreshResponse.get("accessToken").asText();

        mockMvc.perform(
                get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("diana"));
    }

    @Test
    void shouldRotateRefreshToken() throws Exception {

        JsonNode loginResponse = login();

        String oldRefreshToken = loginResponse.get("refreshToken").asText();

        JsonNode refreshResponse = refresh(oldRefreshToken);

        String newRefreshToken = refreshResponse.get("refreshToken").asText();

        assertThat(newRefreshToken).isNotBlank().isNotEqualTo(oldRefreshToken);
    }

    @Test
    void shouldRejectAlreadyUsedRefreshToken() throws Exception {

        JsonNode loginResponse = login();

        String oldRefreshToken = loginResponse.get("refreshToken").asText();

        refresh(oldRefreshToken);

        String secondRefreshBody = """
            {
              "refreshToken": "%s"
            }
            """.formatted(oldRefreshToken);

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
            .content(secondRefreshBody)).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception {

        String body = """
            {
              "refreshToken": "invalid-refresh-token"
            }
            """;

        mockMvc.perform(
                post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private JsonNode login() throws Exception {

        String body = """
            {
              "username": "diana",
              "password": "diana123"
            }
            """;

        var result = mockMvc.perform(
                post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode refresh(String refreshToken) throws Exception {

        String body = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        var result = mockMvc.perform(
                post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() throws Exception {

        JsonNode loginResponse = login();

        String refreshToken = loginResponse.get("refreshToken").asText();

        String logoutBody = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(logoutBody))
            .andExpect(status().isNoContent());

        String refreshBody = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshBody))
            .andExpect(status().isUnauthorized());
    }
}