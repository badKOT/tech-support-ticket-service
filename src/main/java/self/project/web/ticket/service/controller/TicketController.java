package self.project.web.ticket.service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/projects/{projectId}/tickets")
    public List<TicketResponse> getTickets(
        @PathVariable Long projectId
    ) {
        log.info("[GET /projects/{}/tickets] Got request", projectId);

        return ticketService.getTicketsByProject(projectId);
    }

    @GetMapping("/tickets/{ticketId}")
    public TicketResponse getTicket(
        @PathVariable Long ticketId
    ) {
        log.info("[GET /tickets/{}] Got request", ticketId);

        return ticketService.getTicket(ticketId);
    }

    @PostMapping("/projects/{projectId}/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(
        @PathVariable Long projectId,
        @Valid @RequestBody TicketRequest request
    ) {
        log.info("[POST /projects/{}/tickets] Got request, body = {}", projectId, request);

        return ticketService.createTicket(projectId, request);
    }

    @PutMapping("/tickets/{ticketId}")
    public TicketResponse updateTicket(
        @PathVariable Long ticketId,
        @Valid @RequestBody TicketUpdateRequest request
    ) {
        log.info("[PUT /tickets/{}] Got request, body = {}", ticketId, request);

        return ticketService.updateTicket(ticketId, request);
    }

    @PatchMapping("/tickets/{ticketId}/status")
    public TicketResponse changeStatus(
        @PathVariable Long ticketId,
        @Valid @RequestBody TicketStatusUpdateRequest request
    ) {
        log.info("[PATCH /tickets/{}/status] Got request, body = {}", ticketId, request);

        return ticketService.changeStatus(ticketId, request);
    }

    @GetMapping("/ticket-assignees")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    public List<UserResponse> getAvailableAssignees() {
        log.info("[GET /ticket-assignees] Got request");

        return ticketService.getAvailableAssignees();
    }

    @PatchMapping("/tickets/{ticketId}/assignee")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    public TicketResponse assignTicket(
        @PathVariable Long ticketId,
        @RequestBody TicketAssigneeUpdateRequest request
    ) {
        log.info("[PATCH /tickets/{}/assignee] Got request, body = {}", ticketId, request);

        return ticketService.assignTicket(ticketId, request);
    }

    @DeleteMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicket(
        @PathVariable Long ticketId
    ) {
        log.info("[DELETE /tickets/{}] Got request", ticketId);

        ticketService.deleteTicket(ticketId);
    }

    @GetMapping("/tickets/{ticketId}/comments")
    public List<CommentResponse> getComments(
        @PathVariable Long ticketId
    ) {
        log.info("[GET /tickets/{}/comments] Got request", ticketId);

        return ticketService.getComments(ticketId);
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
        @PathVariable Long ticketId,
        @Valid @RequestBody CommentRequest request
    ) {
        log.info("[POST /tickets/{}/comments] Got request, body = {}", ticketId, request);

        return ticketService.addComment(ticketId, request);
    }

    @GetMapping("/projects/{projectId}/analytics")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    public ProjectAnalytics getProjectAnalytics(
        @PathVariable Long projectId
    ) {
        log.info("[GET /projects/{}/analytics] Got request", projectId);

        return ticketService.getProjectAnalytics(projectId);
    }
}