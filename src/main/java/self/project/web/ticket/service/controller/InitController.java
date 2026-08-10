package self.project.web.ticket.service.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import self.project.web.ticket.service.entity.*;
import self.project.web.ticket.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import self.project.web.ticket.service.service.PasswordService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class InitController {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final TicketRepository ticketRepo;
    private final CommentRepository commentRepo;
    private final PasswordService passwordService;

    @GetMapping("/init-db")
    @Transactional
    public String initDb() {
        log.info("[GET /init-db] Got request!");

        if (userRepo.count() > 0) {
            return "Database already initialized.";
        }

        User alice = createUser(
            "alice",
            "Alice Johnson",
            "alice@example.com",
            "alice123",
            UserRole.REQUESTER
        );
        User bob = createUser(
            "bob",
            "Bob Smith",
            "bob@example.com",
            "bob12345",
            UserRole.SUPPORT_AGENT
        );
        User charlie = createUser(
            "charlie",
            "Charlie Brown",
            "charlie@example.com",
            "charlie123",
            UserRole.TEAM_LEAD
        );
        User diana = createUser(
            "diana",
            "Diana Prince",
            "diana@example.com",
            "diana123",
            UserRole.ADMIN
        );

        Project it = projectRepo.save(
            new Project(
                "IT Support",
                "IT",
                "Internal IT support and infrastructure issues"
            )
        );
        Project eng = projectRepo.save(
            new Project(
                "Engineering",
                "ENG",
                "Software engineering tasks and bug tracking"
            )
        );
        Project hr = projectRepo.save(
            new Project(
                "HR Requests",
                "HR",
                "Human resources and onboarding requests"
            )
        );

        Instant now = Instant.now();

        Ticket t1 = createTicket(
            "Laptop not booting",
            "My work laptop shows a black screen when I press the power button.",
            it,
            alice,
            null,
            TicketStatus.OPEN,
            now,
            null
        );
        Ticket t2 = createTicket(
            "VPN connection drops",
            "VPN disconnects every 15 minutes during work hours.",
            it,
            charlie,
            bob,
            TicketStatus.IN_PROGRESS,
            now.minus(3, ChronoUnit.DAYS),
            null
        );
        Ticket t3 = createTicket(
            "Set up new monitor",
            "Need a second monitor set up at desk 42B.",
            it,
            diana,
            null,
            TicketStatus.OPEN,
            now,
            null
        );
        Ticket t4 = createTicket(
            "Fix login page bug",
            "Users cannot log in when password contains special characters.",
            eng,
            bob,
            charlie,
            TicketStatus.OPEN,
            now.minus(1, ChronoUnit.DAYS),
            null
        );
        Ticket t5 = createTicket(
            "Update CI pipeline",
            "Add automated testing for the new microservice.",
            eng,
            alice,
            bob,
            TicketStatus.IN_PROGRESS,
            now.minus(5, ChronoUnit.DAYS),
            null
        );
        Ticket t6 = createTicket(
            "New hire onboarding",
            "Please provision accounts for Jane Doe starting next Monday.",
            hr,
            charlie,
            null,
            TicketStatus.OPEN,
            now,
            null
        );

        createTicket(
            "WiFi keeps dropping in meeting room B",
            "Unstable connection during meetings, seems to affect all devices.",
            it,
            bob,
            diana,
            TicketStatus.IN_PROGRESS,
            now.minus(2, ChronoUnit.DAYS),
            null
        );
        createTicket(
            "Printer out of toner",
            "Floor 3 printer (HP-4200) needs new black toner cartridge.",
            it,
            alice,
            null,
            TicketStatus.OPEN,
            now.minus(1, ChronoUnit.DAYS),
            null
        );
        createTicket(
            "Email server slow",
            "Outlook takes 30+ seconds to load inbox every morning.",
            it,
            charlie,
            bob,
            TicketStatus.IN_REVIEW,
            now.minus(6, ChronoUnit.DAYS),
            null
        );
        createTicket(
            "Password reset for contractor",
            "External contractor (John) locked out of his account.",
            it,
            diana,
            alice,
            TicketStatus.RESOLVED,
            now.minus(4, ChronoUnit.DAYS),
            now.minus(3, ChronoUnit.DAYS)
        );
        createTicket(
            "Request admin access to DB",
            "Need read-only access to production DB for audit purposes.",
            it,
            bob,
            null,
            TicketStatus.NEED_INFO,
            now.minus(7, ChronoUnit.DAYS),
            null
        );
        createTicket(
            "Office chair broken",
            "Chair at desk 15C has a broken armrest, needs replacement.",
            it,
            alice,
            diana,
            TicketStatus.CLOSED,
            now.minus(10, ChronoUnit.DAYS),
            now.minus(8, ChronoUnit.DAYS)
        );
        createTicket(
            "VPN token expired",
            "My hardware token shows error 503, cannot connect remotely.",
            it,
            charlie,
            bob,
            TicketStatus.CLOSED,
            now.minus(12, ChronoUnit.DAYS),
            now.minus(11, ChronoUnit.DAYS)
        );
        createTicket(
            "Software license renewal",
            "AutoCAD license expiring next week, need procurement approval.",
            it,
            diana,
            alice,
            TicketStatus.REOPENED,
            now.minus(9, ChronoUnit.DAYS),
            now.minus(7, ChronoUnit.DAYS)
        );
        createTicket(
            "Conference room display flickering",
            "HDMI connection unstable on the 4K display in room 2A.",
            it,
            bob,
            null,
            TicketStatus.OPEN,
            now.minus(1, ChronoUnit.DAYS),
            null
        );
        createTicket(
            "Laptop upgrade request",
            "Current laptop is 4 years old, requesting upgrade to M3 model.",
            it,
            alice,
            diana,
            TicketStatus.CLOSED,
            now.minus(14, ChronoUnit.DAYS),
            now.minus(12, ChronoUnit.DAYS)
        );
        createTicket(
            "API rate limiting not working",
            "GET /users endpoint is not throttled, returning 200 at any rate.",
            eng,
            charlie,
            bob,
            TicketStatus.IN_PROGRESS,
            now.minus(2, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Memory leak in auth service",
            "Pod restarts every 2 hours with OOMKilled, heap dump attached.",
            eng,
            bob,
            charlie,
            TicketStatus.IN_REVIEW,
            now.minus(4, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Add dark mode toggle",
            "Users requesting dark mode for the admin dashboard.",
            eng,
            alice,
            null,
            TicketStatus.OPEN,
            now.minus(3, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Fix flaky e2e test",
            "Checkout flow test fails ~30% of the time on CI, passes locally.",
            eng,
            charlie,
            bob,
            TicketStatus.IN_PROGRESS,
            now.minus(8, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Database migration script",
            "Write migration for new user_preferences table (milestone 4).",
            eng,
            bob,
            null,
            TicketStatus.RESOLVED,
            now.minus(7, ChronoUnit.DAYS),
            now.minus(5, ChronoUnit.DAYS)
        );

        createTicket(
            "CDN cache invalidation",
            "Static assets not updating after deploy, need cache busting strategy.",
            eng,
            alice,
            charlie,
            TicketStatus.CLOSED,
            now.minus(13, ChronoUnit.DAYS),
            now.minus(10, ChronoUnit.DAYS)
        );

        createTicket(
            "Upgrade Spring Boot to 4.2",
            "Plan and execute framework upgrade across all microservices.",
            eng,
            charlie,
            null,
            TicketStatus.OPEN,
            now.minus(1, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "PagerDuty integration broken",
            "Alerts not triggering on-call rotation since last deploy.",
            eng,
            bob,
            alice,
            TicketStatus.CLOSED,
            now.minus(11, ChronoUnit.DAYS),
            now.minus(9, ChronoUnit.DAYS)
        );

        createTicket(
            "Onboard summer interns",
            "5 interns starting June 1st, need accounts, laptops, and badges.",
            hr,
            diana,
            charlie,
            TicketStatus.IN_PROGRESS,
            now.minus(5, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Update employee handbook",
            "Section 4.2 on remote work policy needs 2026 revisions.",
            hr,
            alice,
            null,
            TicketStatus.NEED_INFO,
            now.minus(6, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Arrange team building event",
            "Book venue and catering for Q3 offsite (~40 people).",
            hr,
            charlie,
            diana,
            TicketStatus.IN_REVIEW,
            now.minus(9, ChronoUnit.DAYS),
            null
        );

        createTicket(
            "Process contractor invoice",
            "Invoice #INV-2026-047 for UX consultant needs approval workflow.",
            hr,
            bob,
            alice,
            TicketStatus.CLOSED,
            now.minus(8, ChronoUnit.DAYS),
            now.minus(6, ChronoUnit.DAYS)
        );


        commentRepo.save(
            new Comment(
                "Have you tried holding the power button for 10 seconds?",
                t1,
                bob
            )
        );

        commentRepo.save(
            new Comment(
                "Yes, still nothing. The charging light is on though.",
                t1,
                alice
            )
        );

        commentRepo.save(
            new Comment(
                "I am investigating the issue with the network team.",
                t2,
                bob
            )
        );

        commentRepo.save(
            new Comment(
                "I can reproduce this on staging. Working on a fix.",
                t4,
                charlie
            )
        );

        String result = "Database initialized!";

        log.info("[GET /init-db] Sending response: {}", result);

        return result;
    }


    private User createUser(
        String username,
        String displayName,
        String email,
        String password,
        UserRole role
    ) {
        PasswordService.PasswordData passwordData =
            passwordService.encode(password);

        return userRepo.save(
            new User(
                username,
                displayName,
                email,
                passwordData.passwordHash(),
                passwordData.salt(),
                role
            )
        );
    }

    private Ticket createTicket(
        String title,
        String description,
        Project project,
        User creator,
        User assignee,
        TicketStatus status,
        Instant createdAt,
        Instant closedAt
    ) {
        Ticket ticket = new Ticket(
            title,
            description,
            project,
            creator
        );

        ticket.setStatus(status);

        if (assignee != null) {
            ticket.setAssignee(assignee);
        }

        ticket = ticketRepo.save(ticket);

        ticketRepo.updateCreatedAt(
            ticket.getId(),
            createdAt
        );

        if (closedAt != null) {
            ticketRepo.updateClosedAt(
                ticket.getId(),
                closedAt
            );
        }

        return ticketRepo
            .findById(ticket.getId())
            .orElseThrow();
    }
}

