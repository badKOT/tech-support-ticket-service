package self.project.web.ticket.service.dto;
import java.time.Instant;
import self.project.web.ticket.service.entity.TicketActivity;
public record TicketActivityResponse(Long id, Long actorId, String actorName, String field,
        String oldValue, String newValue, Instant createdAt) {
    public static TicketActivityResponse from(TicketActivity a) {
        return new TicketActivityResponse(a.getId(), a.getActorId(), a.getActorName(), a.getField(),
                a.getOldValue(), a.getNewValue(), a.getCreatedAt());
    }
}
