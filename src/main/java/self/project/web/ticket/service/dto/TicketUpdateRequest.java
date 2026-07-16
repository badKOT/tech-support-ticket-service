package self.project.web.ticket.service.dto;

public record TicketUpdateRequest(
        String title,
        String description,
        String status,
        Long assigneeId,
        Long projectId) {}
