package authorization;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import self.project.web.ticket.service.Runner;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.entity.UserRole;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.service.PasswordService;
import self.project.web.ticket.service.repository.RefreshTokenRepository;

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

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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

        mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
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

        mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(body)).andExpect(status().isUnauthorized());
    }


    @Test
    void shouldRehashPasswordAfterSuccessfulLogin() throws Exception {

        User beforeLogin = userRepository.findByUsername("diana").orElseThrow();
        String oldHash = beforeLogin.getPasswordHash();
        String oldSalt = beforeLogin.getSalt();

        String body = """
            {
              "username": "diana",
              "password": "diana123"
            }
            """;

        mockMvc.perform(
                post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());

        User afterLogin = userRepository.findByUsername("diana").orElseThrow();

        assertThat(afterLogin.getSalt()).isNotEqualTo(oldSalt);
        assertThat(afterLogin.getPasswordHash()).isNotEqualTo(oldHash);
    }

    @Test
    void shouldNotRehashPasswordAfterFailedLogin() throws Exception {
        User beforeLogin = userRepository.findByUsername("diana").orElseThrow();

        String oldHash = beforeLogin.getPasswordHash();
        String oldSalt = beforeLogin.getSalt();

        String body = """
            {
              "username": "diana",
              "password": "wrong-password"
            }
            """;

        mockMvc.perform(
                post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());

        User afterLogin = userRepository.findByUsername("diana").orElseThrow();

        assertThat(afterLogin.getPasswordHash()).isEqualTo(oldHash);
        assertThat(afterLogin.getSalt()).isEqualTo(oldSalt);
    }
}
