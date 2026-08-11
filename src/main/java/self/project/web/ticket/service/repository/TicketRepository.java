package self.project.web.ticket.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import self.project.web.ticket.service.entity.Ticket;

import java.time.Instant;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Ticket> findByProjectIdAndCreatorIdOrderByCreatedAtDesc(
        Long projectId,
        Long creatorId
    );

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Ticket t
        SET t.createdAt = :ts
        WHERE t.id = :id
        """)
    void updateCreatedAt(
        @Param("id") Long id,
        @Param("ts") Instant ts
    );

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Ticket t
        SET t.closedAt = :ts
        WHERE t.id = :id
        """)
    void updateClosedAt(
        @Param("id") Long id,
        @Param("ts") Instant ts
    );

    @Query("""
        SELECT t
        FROM Ticket t
        WHERE t.project.id = :projectId
          AND (
              t.assignee.id = :userId
              OR t.creator.id = :userId
          )
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findVisibleToSupportAgent(
        @Param("projectId") Long projectId,
        @Param("userId") Long userId
    );
}