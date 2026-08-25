package self.project.web.ticket.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import self.project.web.ticket.service.dto.ProjectResponse;
import self.project.web.ticket.service.dto.UserResponse;
import self.project.web.ticket.service.repository.ProjectRepository;
import self.project.web.ticket.service.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommonService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;

    public List<ProjectResponse> getProjects() {
        return projectRepo.findAll().stream().map(ProjectResponse::from).toList();
    }

    public List<UserResponse> getUsers() {
        return userRepo.findAll().stream().map(UserResponse::from).toList();
    }
}