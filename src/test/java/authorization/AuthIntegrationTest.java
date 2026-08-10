package authorization;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import self.project.web.ticket.service.Runner;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.entity.UserRole;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.service.PasswordService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        PasswordService.PasswordData passwordData =
            passwordService.encode("diana123");

        User diana = new User(
            "diana",
            "Diana Prince",
            "diana@example.com",
            passwordData.passwordHash(),
            passwordData.salt(),
            UserRole.ADMIN
        );

        userRepository.save(diana);
    }

    @Test
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(
            get("/api/auth/me")
        ).andExpect(status().isUnauthorized());
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
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("diana"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.enabled").value(true));
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
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldStoreAuthenticationInSession() throws Exception {
        String body = """
            {
              "username": "diana",
              "password": "diana123"
            }
            """;

        var loginResult = mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andReturn();

        HttpSession session =
            loginResult
                .getRequest()
                .getSession(false);

        assertThat(session).isNotNull();

        mockMvc.perform(
                get("/api/auth/me")
                    .session(
                        (org.springframework.mock.web.MockHttpSession)
                            session
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("diana"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
