package self.project.web.ticket.service.dto;

import jakarta.validation.constraints.NotNull;
import self.project.web.ticket.service.entity.TicketStatus;

public record TicketStatusUpdateRequest(
    @NotNull(message = "Status must not be null")
    TicketStatus status
) {

}
