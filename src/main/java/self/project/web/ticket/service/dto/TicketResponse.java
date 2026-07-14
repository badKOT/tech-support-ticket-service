package self.project.web.ticket.service.dto;

import self.project.web.ticket.service.entity.Ticket;
import self.project.web.ticket.service.entity.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeName,
        Long creatorId,
        String creatorName,
        Instant createdAt,
        List<CommentResponse> comments) {

    public static TicketResponse from(Ticket ticket) {
        List<CommentResponse> commentResponses = ticket.getComments().stream()
                .map(CommentResponse::from)
                .toList();
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getProject().getId(),
                ticket.getProject().getName(),
                ticket.getAssignee() != null ? ticket.getAssignee().getId() : null,
                ticket.getAssignee() != null ? ticket.getAssignee().getDisplayName() : null,
                ticket.getCreator().getId(),
                ticket.getCreator().getDisplayName(),
                ticket.getCreatedAt(),
                commentResponses);
    }
}