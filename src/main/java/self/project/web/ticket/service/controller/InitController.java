package self.project.web.ticket.service.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import self.project.web.ticket.service.entity.*;
import self.project.web.ticket.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class InitController {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final TicketRepository ticketRepo;
    private final CommentRepository commentRepo;

    @GetMapping("/init-db")
    @Transactional
    public String initDb() {
        log.info("[GET /init-db] Got request!");
        if (userRepo.count() > 0) {
            return "Database already initialized.";
        }

        User alice = userRepo.save(new User("alice", "Alice Johnson", "alice@example.com"));
        User bob   = userRepo.save(new User("bob", "Bob Smith", "bob@example.com"));
        User charlie = userRepo.save(new User("charlie", "Charlie Brown", "charlie@example.com"));
        User diana = userRepo.save(new User("diana", "Diana Prince", "diana@example.com"));

        Project it  = projectRepo.save(new Project("IT Support", "IT", "Internal IT support and infrastructure issues"));
        Project eng = projectRepo.save(new Project("Engineering", "ENG", "Software engineering tasks and bug tracking"));
        Project hr  = projectRepo.save(new Project("HR Requests", "HR", "Human resources and onboarding requests"));

        Ticket t1 = ticketRepo.save(new Ticket("Laptop not booting", "My work laptop shows a black screen when I press the power button.", it, alice));
        Ticket t2 = ticketRepo.save(new Ticket("VPN connection drops", "VPN disconnects every 15 minutes during work hours.", it, charlie));
        t2.setStatus(TicketStatus.IN_PROGRESS);
        t2.setAssignee(bob);
        ticketRepo.save(t2);

        ticketRepo.save(new Ticket("Set up new monitor", "Need a second monitor set up at desk 42B.", it, diana));

        Ticket t4 = ticketRepo.save(new Ticket("Fix login page bug", "Users cannot log in when password contains special characters.", eng, bob));
        t4.setAssignee(charlie);
        ticketRepo.save(t4);

        Ticket t5 = ticketRepo.save(new Ticket("Update CI pipeline", "Add automated testing for the new microservice.", eng, alice));
        t5.setStatus(TicketStatus.IN_PROGRESS);
        t5.setAssignee(bob);
        ticketRepo.save(t5);

        ticketRepo.save(new Ticket("New hire onboarding", "Please provision accounts for Jane Doe starting next Monday.", hr, charlie));

        commentRepo.save(new Comment("Have you tried holding the power button for 10 seconds?", t1, bob));
        commentRepo.save(new Comment("Yes, still nothing. The charging light is on though.", t1, alice));
        commentRepo.save(new Comment("I am investigating the issue with the network team.", t2, bob));
        commentRepo.save(new Comment("I can reproduce this on staging. Working on a fix.", t4, charlie));

        var result = "Database initialized!";
        log.info("[GET /init-db] Sending response: {}", result);
        return result;
    }
}
