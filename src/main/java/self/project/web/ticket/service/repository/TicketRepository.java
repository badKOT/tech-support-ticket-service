package self.project.web.ticket.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import self.project.web.ticket.service.entity.Ticket;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}