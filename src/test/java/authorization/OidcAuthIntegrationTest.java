package authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import self.project.web.ticket.service.Runner;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.entity.UserRole;
import self.project.web.ticket.service.repository.RefreshTokenRepository;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.service.PasswordService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Runner.class, properties = {"security.oidc.enabled=true",
    "app.frontend-url=http://localhost:5173",

    "spring.security.oauth2.client.registration.support-oidc.provider=support-oidc",
    "spring.security.oauth2.client.registration.support-oidc.client-id=test-client",
    "spring.security.oauth2.client.registration.support-oidc.client-secret=test-secret",
    "spring.security.oauth2.client.registration.support-oidc.authorization-grant-type=authorization_code",
    "spring.security.oauth2.client.registration.support-oidc.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
    "spring.security.oauth2.client.registration.support-oidc.scope=openid,profile,email",

    "spring.security.oauth2.client.provider.support-oidc.authorization-uri=http://localhost/authorize",
    "spring.security.oauth2.client.provider.support-oidc.token-uri=http://localhost/token",
    "spring.security.oauth2.client.provider.support-oidc.jwk-set-uri=http://localhost/jwks",
    "spring.security.oauth2.client.provider.support-oidc.user-info-uri=http://localhost/userinfo",
    "spring.security.oauth2.client.provider.support-oidc.user-name-attribute=sub"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OidcAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordService passwordService;

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
    void shouldStartOidcAuthorizationFlow() throws Exception {

        mockMvc.perform(get("/oauth2/authorization/support-oidc"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("http://localhost/authorize")));
    }

    @Test
    void shouldExchangeOidcSessionForJwtTokens() throws Exception {

        MockHttpSession session = new MockHttpSession();

        session.setAttribute("OIDC_EMAIL", "diana@example.com");

        mockMvc.perform(post("/api/auth/oidc/token").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.username").value("diana"))
            .andExpect(jsonPath("$.user.role").value("ADMIN"));

        assertThatThrownBy(() -> session.getAttribute("OIDC_EMAIL")).isInstanceOf(
            IllegalStateException.class);
    }

    @Test
    void shouldRejectOidcTokenExchangeWithoutSession() throws Exception {

        mockMvc.perform(post("/api/auth/oidc/token")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectOidcSessionWithoutEmail() throws Exception {

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/oidc/token").session(session))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectOidcUserNotPresentInLocalDatabase() throws Exception {

        MockHttpSession session = new MockHttpSession();

        session.setAttribute("OIDC_EMAIL", "unknown@example.com");

        mockMvc.perform(post("/api/auth/oidc/token").session(session))
            .andExpect(status().isUnauthorized());
    }
}