package self.project.web.ticket.service.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import self.project.web.ticket.service.entity.TicketActivity;
import java.util.List;
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {
    List<TicketActivity> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
