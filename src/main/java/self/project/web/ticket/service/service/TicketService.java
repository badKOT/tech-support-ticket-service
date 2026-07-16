package self.project.web.ticket.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.entity.*;
import self.project.web.ticket.service.repository.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;

    public List<TicketResponse> getTicketsByProject(Long projectId) {
        return ticketRepo.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(TicketResponse::from)
                .toList();
    }

    public TicketResponse getTicket(Long ticketId) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse createTicket(Long projectId, TicketRequest request) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        User creator = userRepo.findById(request.creatorId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.creatorId()));
        Ticket ticket = new Ticket(request.title(), request.description(), project, creator);
        ticket = ticketRepo.save(ticket);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long ticketId, TicketUpdateRequest request) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        if (request.title() != null && !request.title().isBlank()) {
            ticket.setTitle(request.title());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.status() != null) {
            TicketStatus newStatus = TicketStatus.valueOf(request.status());
            if (newStatus == TicketStatus.CLOSED && ticket.getStatus() != TicketStatus.CLOSED) {
                ticket.setClosedAt(Instant.now());
            }
            ticket.setStatus(newStatus);
        }
        if (request.assigneeId() != null) {
            User assignee = userRepo.findById(request.assigneeId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.assigneeId()));
            ticket.setAssignee(assignee);
        } else {
            ticket.setAssignee(null);
        }
        if (request.projectId() != null) {
            Project project = projectRepo.findById(request.projectId())
                    .orElseThrow(() -> new RuntimeException("Project not found: " + request.projectId()));
            ticket.setProject(project);
        }

        ticket = ticketRepo.save(ticket);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest request) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        User author = userRepo.findById(request.authorId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.authorId()));
        Comment comment = new Comment(request.content(), ticket, author);
        comment = commentRepo.save(comment);
        return CommentResponse.from(comment);
    }

    public List<CommentResponse> getComments(Long ticketId) {
        return commentRepo.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    public ProjectAnalytics getProjectAnalytics(Long projectId) {
        List<Ticket> tickets = ticketRepo.findByProjectIdOrderByCreatedAtDesc(projectId);
        ZoneId zone = ZoneId.systemDefault();

        Map<String, Long> statusCounts = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        LocalDate today = LocalDate.now();
        List<DailyCount> dailyCounts = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            long created = tickets.stream()
                    .filter(t -> t.getCreatedAt().atZone(zone).toLocalDate().equals(date))
                    .count();
            long resolved = tickets.stream()
                    .filter(t -> t.getClosedAt() != null && t.getClosedAt().atZone(zone).toLocalDate().equals(date))
                    .count();
            dailyCounts.add(new DailyCount(dateStr, created, resolved));
        }

        List<AssigneeResolutionTime> resolutionTimes = tickets.stream()
                .filter(t -> t.getClosedAt() != null && t.getAssignee() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAssignee().getDisplayName(),
                        Collectors.averagingDouble(t ->
                                Duration.between(t.getCreatedAt(), t.getClosedAt()).toHours()
                        )
                ))
                .entrySet().stream()
                .map(e -> new AssigneeResolutionTime(e.getKey(), Math.round(e.getValue() * 10.0) / 10.0))
                .toList();

        return new ProjectAnalytics(statusCounts, dailyCounts, resolutionTimes);
    }
}