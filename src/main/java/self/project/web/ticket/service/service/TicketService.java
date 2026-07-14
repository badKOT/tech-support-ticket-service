package self.project.web.ticket.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.entity.*;
import self.project.web.ticket.service.repository.*;

import java.util.List;

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
            ticket.setStatus(TicketStatus.valueOf(request.status()));
        }
        if (request.assigneeId() != null) {
            User assignee = userRepo.findById(request.assigneeId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.assigneeId()));
            ticket.setAssignee(assignee);
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
}