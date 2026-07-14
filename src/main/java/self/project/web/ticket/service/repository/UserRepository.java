package self.project.web.ticket.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import self.project.web.ticket.service.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}