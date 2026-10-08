package self.project.web.ticket.service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_activities", indexes = @Index(columnList = "ticket_id"))
@Getter
@NoArgsConstructor
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(nullable = false)
    private Long actorId;
    @Column(nullable = false)
    private String actorName;
    @Column(nullable = false)
    private String field;
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    @Column(columnDefinition = "TEXT")
    private String newValue;
    @Column(nullable = false)
    private Instant createdAt;

    public TicketActivity(Ticket ticket, User actor, String field, String oldValue, String newValue) {
        this.ticket = ticket;
        this.actorId = actor.getId();
        this.actorName = actor.getDisplayName();
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = Instant.now();
    }
}
