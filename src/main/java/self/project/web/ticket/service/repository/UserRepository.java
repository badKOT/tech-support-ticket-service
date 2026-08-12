package self.project.web.ticket.service.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    List<User> findAllByEnabledTrueAndRoleIn(Collection<UserRole> roles);
}