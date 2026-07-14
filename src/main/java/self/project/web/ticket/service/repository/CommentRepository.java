package self.project.web.ticket.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import self.project.web.ticket.service.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}