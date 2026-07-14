package self.project.web.ticket.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import self.project.web.ticket.service.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}