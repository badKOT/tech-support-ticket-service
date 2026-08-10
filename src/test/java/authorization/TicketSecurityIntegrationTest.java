package authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import self.project.web.ticket.service.Runner;
import self.project.web.ticket.service.entity.Project;
import self.project.web.ticket.service.entity.Ticket;
import self.project.web.ticket.service.entity.UserRole;
import self.project.web.ticket.service.repository.CommentRepository;
import self.project.web.ticket.service.repository.ProjectRepository;
import self.project.web.ticket.service.repository.TicketRepository;
import self.project.web.ticket.service.repository.UserRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.service.PasswordService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Runner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordService passwordService;

    private User alice;
    private User eve;
    private User bob;
    private User charlie;
    private User diana;

    private Project project;

    private Ticket aliceTicket;

    @BeforeEach
    void setUp() {
        /*
         * Сначала очищаем зависимые сущности.
         */
        commentRepository.deleteAll();
        ticketRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        alice = createUser(
            "alice",
            "Alice Johnson",
            UserRole.REQUESTER
        );

        eve = createUser(
            "eve",
            "Eve Adams",
            UserRole.REQUESTER
        );

        bob = createUser(
            "bob",
            "Bob Smith",
            UserRole.SUPPORT_AGENT
        );

        charlie = createUser(
            "charlie",
            "Charlie Brown",
            UserRole.TEAM_LEAD
        );

        diana = createUser(
            "diana",
            "Diana Prince",
            UserRole.ADMIN
        );

        project = projectRepository.save(
            new Project(
                "Support",
                "SUP",
                "Support project"
            )
        );

        aliceTicket = new Ticket(
            "Cannot login",
            "Login page does not work",
            project,
            alice
        );

        aliceTicket.setAssignee(bob);

        aliceTicket = ticketRepository.save(aliceTicket);
    }

    @Test
    void requesterShouldSeeOwnTicket() throws Exception {
        mockMvc.perform(
                get("/api/tickets/{id}", aliceTicket.getId())
                    .with(user("alice").roles("REQUESTER"))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(aliceTicket.getId())
            );
    }

    @Test
    void requesterShouldNotSeeAnotherUsersTicket() throws Exception {
        mockMvc.perform(
                get("/api/tickets/{id}", aliceTicket.getId())
                    .with(user("eve").roles("REQUESTER"))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void assignedAgentShouldSeeTicket() throws Exception {
        mockMvc.perform(
                get("/api/tickets/{id}", aliceTicket.getId())
                    .with(user("bob").roles("SUPPORT_AGENT"))
            )
            .andExpect(status().isOk());
    }

    @Test
    void teamLeadShouldSeeTicket() throws Exception {
        mockMvc.perform(
                get("/api/tickets/{id}", aliceTicket.getId())
                    .with(user("charlie").roles("TEAM_LEAD"))
            )
            .andExpect(status().isOk());
    }

    @Test
    void adminShouldSeeAnyTicket() throws Exception {
        mockMvc.perform(
                get("/api/tickets/{id}", aliceTicket.getId())
                    .with(user("diana").roles("ADMIN"))
            )
            .andExpect(status().isOk());
    }

    @Test
    void requesterShouldCreateTicketAsHimself() throws Exception {
        String body = """
            {
              "title": "New ticket",
              "description": "Something is broken"
            }
            """;

        mockMvc.perform(
                post(
                    "/api/projects/{projectId}/tickets",
                    project.getId()
                )
                    .with(user("alice").roles("REQUESTER"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated());

        Ticket createdTicket = ticketRepository.findAll()
            .stream()
            .filter(ticket ->
                "New ticket".equals(ticket.getTitle())
            )
            .findFirst()
            .orElseThrow();

        assertThat(createdTicket.getCreator().getId())
            .isEqualTo(alice.getId());
    }

    @Test
    void requesterShouldNotAssignAgent() throws Exception {
        String body = """
            {
              "assigneeId": %d
            }
            """.formatted(bob.getId());

        mockMvc.perform(
                patch(
                    "/api/tickets/{ticketId}/assignee",
                    aliceTicket.getId()
                )
                    .with(user("alice").roles("REQUESTER"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void teamLeadShouldAssignAgent() throws Exception {
        aliceTicket.setAssignee(null);
        ticketRepository.save(aliceTicket);

        String body = """
            {
              "assigneeId": %d
            }
            """.formatted(bob.getId());

        mockMvc.perform(
                patch(
                    "/api/tickets/{ticketId}/assignee",
                    aliceTicket.getId()
                )
                    .with(user("charlie").roles("TEAM_LEAD"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk());

        Ticket updatedTicket = ticketRepository
            .findById(aliceTicket.getId())
            .orElseThrow();

        assertThat(updatedTicket.getAssignee()).isNotNull();
        assertThat(updatedTicket.getAssignee().getId())
            .isEqualTo(bob.getId());
    }

    @Test
    void assignedAgentShouldChangeStatus() throws Exception {
        String body = """
            {
              "status": "CLOSED"
            }
            """;

        mockMvc.perform(
                patch(
                    "/api/tickets/{ticketId}/status",
                    aliceTicket.getId()
                )
                    .with(user("bob").roles("SUPPORT_AGENT"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.status").value("CLOSED")
            );

        Ticket updatedTicket = ticketRepository
            .findById(aliceTicket.getId())
            .orElseThrow();

        assertThat(updatedTicket.getClosedAt()).isNotNull();
    }

    @Test
    void requesterShouldNotChangeStatus() throws Exception {
        String body = """
            {
              "status": "CLOSED"
            }
            """;

        mockMvc.perform(
                patch(
                    "/api/tickets/{ticketId}/status",
                    aliceTicket.getId()
                )
                    .with(user("alice").roles("REQUESTER"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void requestWithoutCsrfShouldBeRejected() throws Exception {
        String body = """
            {
              "title": "CSRF test",
              "description": "This must be rejected"
            }
            """;

        mockMvc.perform(
                post(
                    "/api/projects/{projectId}/tickets",
                    project.getId()
                )
                    .with(user("alice").roles("REQUESTER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isForbidden());
    }

    private User createUser(
        String username,
        String displayName,
        UserRole role
    ) {
        PasswordService.PasswordData passwordData =
            passwordService.encode("password123");

        User user = new User(
            username,
            displayName,
            username + "@example.com",
            passwordData.passwordHash(),
            passwordData.salt(),
            role
        );

        return userRepository.save(user);
    }
}
