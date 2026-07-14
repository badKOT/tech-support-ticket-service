package self.project.web.ticket.service.controller;

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
    public List<TicketResponse> getTickets(@PathVariable Long projectId) {
        log.info("[GET /projects/tickets] Got request! ProjectId = {}", projectId);
        var result = ticketService.getTicketsByProject(projectId);
        log.info("[GET /projects/tickets] Sending response: {}", result);
        return result;
    }

    @GetMapping("/tickets/{ticketId}")
    public TicketResponse getTicket(@PathVariable Long ticketId) {
        log.info("[GET /tickets] Got request! TicketId = {}", ticketId);
        var result = ticketService.getTicket(ticketId);
        log.info("[GET /tickets] Sending response: {}", result);
        return result;
    }

    @PostMapping("/projects/{projectId}/tickets")
    public TicketResponse createTicket(@PathVariable Long projectId, @RequestBody TicketRequest request) {
        log.info("[POST /projects/tickets] Got request! ProjectId = {}, Body = {}", projectId, request);
        var result = ticketService.createTicket(projectId, request);
        log.info("[POST /projects/tickets] Sending response: {}", result);
        return result;
    }

    @PutMapping("/tickets/{ticketId}")
    public TicketResponse updateTicket(@PathVariable Long ticketId, @RequestBody TicketUpdateRequest request) {
        log.info("[PUT /projects/tickets] Got request! ProjectId = {}, Body = {}", ticketId, request);
        var result = ticketService.updateTicket(ticketId, request);
        log.info("[PUT /projects/tickets] Sending response: {}", result);
        return result;
    }

    @GetMapping("/tickets/{ticketId}/comments")
    public List<CommentResponse> getComments(@PathVariable Long ticketId) {
        log.info("[GET /tickets/comments] Got request! TicketId = {}", ticketId);
        var result = ticketService.getComments(ticketId);
        log.info("[GET /tickets/comments] Sending response: {}", result);
        return result;
    }

    @PostMapping("/tickets/{ticketId}/comments")
    public CommentResponse addComment(@PathVariable Long ticketId, @RequestBody CommentRequest request) {
        log.info("[POST /tickets/comments] Got request! TicketId = {}, Body = {}", ticketId, request);
        var result = ticketService.addComment(ticketId, request);
        log.info("[POST /tickets/comments] Sending response: {}", result);
        return result;
    }
}