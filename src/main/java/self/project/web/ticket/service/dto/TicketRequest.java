package self.project.web.ticket.service.dto;

public record TicketRequest(
        String title,
        String description,
        Long creatorId) {
}