package self.project.web.ticket.service.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import self.project.web.ticket.service.entity.Ticket;

import java.time.Instant;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @EntityGraph(attributePaths = {"project", "assignee", "creator"}, type = EntityGraphType.FETCH)
    List<Ticket> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Ticket t SET t.createdAt = :ts WHERE t.id = :id")
    void updateCreatedAt(@Param("id") Long id, @Param("ts") Instant ts);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Ticket t SET t.closedAt = :ts WHERE t.id = :id")
    void updateClosedAt(@Param("id") Long id, @Param("ts") Instant ts);
}