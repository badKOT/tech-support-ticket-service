package self.project.web.ticket.service.dto;

import jakarta.validation.constraints.Size;

public record TicketUpdateRequest(
    @Size(max = 255, message = "Title must contain no more than 255 characters")
    String title,

    @Size(max = 10_000, message = "Description must contain no more than 10000 characters")
    String description,

    Long projectId
) {

}
