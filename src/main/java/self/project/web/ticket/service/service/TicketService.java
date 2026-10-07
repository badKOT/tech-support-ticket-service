package self.project.web.ticket.service.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.entity.*;
import self.project.web.ticket.service.repository.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import self.project.web.ticket.service.security.CurrentUserService;
import self.project.web.ticket.service.security.TicketAccessService;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;
    private final TicketActivityRepository activityRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;
    private final CurrentUserService currentUserService;
    private final TicketAccessService ticketAccessService;

    public List<TicketResponse> getTicketsByProject(Long projectId) {
        getProjectOrThrow(projectId);

        currentUserService.getCurrentUser();

        return ticketRepo.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(TicketResponse::from).toList();
    }

    public TicketResponse getTicket(Long ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User currentUser = currentUserService.getCurrentUser();

        checkCanRead(ticket, currentUser);

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse createTicket(Long projectId, TicketRequest request) {
        Project project = getProjectOrThrow(projectId);
        User creator = currentUserService.getCurrentUser();

        Ticket ticket = new Ticket(request.title().trim(), request.description(), project, creator);

        Ticket savedTicket = ticketRepo.save(ticket);

        return TicketResponse.from(savedTicket);
    }

    @Transactional
    public TicketResponse updateTicket(Long ticketId, TicketUpdateRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User currentUser = currentUserService.getCurrentUser();

        if (!ticketAccessService.canEditContent(ticket, currentUser)) {
            throw new AccessDeniedException("You cannot edit ticket " + ticketId);
        }

        if (request.title() != null) {
            String title = request.title().trim();

            if (title.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ticket title must not be blank");
            }

            recordActivity(ticket, currentUser, "TITLE", ticket.getTitle(), title);
            ticket.setTitle(title);
        }

        if (request.description() != null) {
            recordActivity(ticket, currentUser, "DESCRIPTION", ticket.getDescription(), request.description());
            ticket.setDescription(request.description());
        }

        if (request.projectId() != null && !Objects.equals(request.projectId(),
            ticket.getProject().getId())) {

            if (!ticketAccessService.canMoveTicket(currentUser)) {
                throw new AccessDeniedException("You cannot move a ticket to another project");
            }

            Project project = getProjectOrThrow(request.projectId());

            recordActivity(ticket, currentUser, "PROJECT", ticket.getProject().getKey(), project.getKey());
            ticket.setProject(project);
        }

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse changeStatus(Long ticketId, TicketStatusUpdateRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);

        User currentUser = currentUserService.getCurrentUser();

        TicketStatus oldStatus = ticket.getStatus();

        TicketStatus newStatus = request.status();

        if (!ticketAccessService.canChangeStatus(ticket, currentUser, newStatus)) {
            throw new AccessDeniedException("You cannot change status of ticket " + ticketId);
        }

        recordActivity(ticket, currentUser, "STATUS", oldStatus.name(), newStatus.name());
        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.CLOSED && oldStatus != TicketStatus.CLOSED) {
            ticket.setClosedAt(Instant.now());
        }

        if (newStatus != TicketStatus.CLOSED) {
            ticket.setClosedAt(null);
        }

        return TicketResponse.from(ticket);
    }

    public List<UserResponse> getAvailableAssignees() {
        return userRepo.findAllByEnabledTrueAndRoleIn(
                List.of(UserRole.SUPPORT_AGENT, UserRole.TEAM_LEAD)).stream().map(UserResponse::from)
            .toList();
    }

    @Transactional
    public TicketResponse assignTicket(Long ticketId, TicketAssigneeUpdateRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);

        User currentUser = currentUserService.getCurrentUser();
        if (request.assigneeId() == null) {

            if (!ticketAccessService.canManageAssignment(currentUser, null)) {

                throw new AccessDeniedException("You cannot remove ticket assignment " + ticketId);
            }

            recordActivity(ticket, currentUser, "ASSIGNEE", assigneeLabel(ticket.getAssignee()), null);
            ticket.setAssignee(null);

            return TicketResponse.from(ticket);
        }

        User assignee = getUserOrThrow(request.assigneeId());

        if (!ticketAccessService.canManageAssignment(currentUser, assignee)) {

            throw new AccessDeniedException(
                "You cannot assign ticket " + ticketId + " to user " + assignee.getId());
        }

        if (assignee.getRole() != UserRole.SUPPORT_AGENT
            && assignee.getRole() != UserRole.TEAM_LEAD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ticket can only be assigned to a support agent " + "or team lead");
        }

        /*
         * Отключённого пользователя назначать нельзя.
         */
        if (!assignee.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Disabled user cannot be assigned to a ticket");
        }

        recordActivity(ticket, currentUser, "ASSIGNEE", assigneeLabel(ticket.getAssignee()), assigneeLabel(assignee));
        ticket.setAssignee(assignee);

        return TicketResponse.from(ticket);
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrator can delete tickets");
        }

        ticketRepo.delete(ticket);
    }

    public List<CommentResponse> getComments(Long ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User currentUser = currentUserService.getCurrentUser();

        checkCanRead(ticket, currentUser);

        return commentRepo.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
            .map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User author = currentUserService.getCurrentUser();

        checkCanRead(ticket, author);

        Comment comment = new Comment(request.content().trim(), ticket, author);

        Comment savedComment = commentRepo.save(comment);

        return CommentResponse.from(savedComment);
    }

    public ProjectAnalytics getProjectAnalytics(Long projectId) {
        getProjectOrThrow(projectId);

        User currentUser = currentUserService.getCurrentUser();

        if (!ticketAccessService.canViewAnalytics(currentUser)) {
            throw new AccessDeniedException("You cannot view project analytics");
        }

        List<Ticket> tickets = ticketRepo.findByProjectIdOrderByCreatedAtDesc(projectId);

        ZoneId zone = ZoneId.systemDefault();

        Map<String, Long> statusCounts = tickets.stream().collect(
            Collectors.groupingBy(ticket -> ticket.getStatus().name(), Collectors.counting()));

        LocalDate today = LocalDate.now();
        List<DailyCount> dailyCounts = new ArrayList<>();

        for (int daysAgo = 13; daysAgo >= 0; daysAgo--) {
            LocalDate date = today.minusDays(daysAgo);

            long created = tickets.stream()
                .filter(ticket -> ticket.getCreatedAt().atZone(zone).toLocalDate().equals(date))
                .count();

            long resolved = tickets.stream().filter(
                ticket -> ticket.getClosedAt() != null && ticket.getClosedAt().atZone(zone)
                    .toLocalDate().equals(date)).count();

            dailyCounts.add(new DailyCount(date.toString(), created, resolved));
        }

        List<AssigneeResolutionTime> resolutionTimes = tickets.stream()
            .filter(ticket -> ticket.getClosedAt() != null && ticket.getAssignee() != null).collect(
                Collectors.groupingBy(ticket -> ticket.getAssignee().getDisplayName(),
                    Collectors.averagingDouble(
                        ticket -> Duration.between(ticket.getCreatedAt(), ticket.getClosedAt())
                            .toHours()))).entrySet().stream().map(
                entry -> new AssigneeResolutionTime(entry.getKey(),
                    Math.round(entry.getValue() * 10.0) / 10.0)).toList();

        return new ProjectAnalytics(statusCounts, dailyCounts, resolutionTimes);
    }

    public List<TicketActivityResponse> getActivity(Long ticketId) {
        checkCanRead(getTicketOrThrow(ticketId), currentUserService.getCurrentUser());
        return activityRepo.findByTicketIdOrderByCreatedAtAscIdAsc(ticketId).stream()
                .map(TicketActivityResponse::from).toList();
    }

    private String assigneeLabel(User user) {
        return user == null ? null : user.getDisplayName() + " (#" + user.getId() + ")";
    }

    private void recordActivity(Ticket ticket, User actor, String field, String before, String after) {
        if (!Objects.equals(before, after)) {
            activityRepo.save(new TicketActivity(ticket, actor, field, before, after));
        }
    }

    private Ticket getTicketOrThrow(Long ticketId) {
        return ticketRepo.findById(ticketId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Ticket not found: " + ticketId));
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepo.findById(projectId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Project not found: " + projectId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepo.findById(userId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private void checkCanRead(Ticket ticket, User currentUser) {
        if (!ticketAccessService.canRead(ticket, currentUser)) {
            throw new AccessDeniedException("You do not have access to ticket " + ticket.getId());
        }
    }
}