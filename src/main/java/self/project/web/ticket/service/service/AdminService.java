package self.project.web.ticket.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.entity.Project;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.ProjectRepository;
import self.project.web.ticket.service.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;

    public List<UserResponse> getUsers() {
        return userRepo.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = new User(request.username(), request.displayName(), request.email());
        user = userRepo.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        if (request.username() != null && !request.username().isBlank()) {
            user.setUsername(request.username());
        }
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        user = userRepo.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        userRepo.deleteById(id);
    }

    public List<ProjectResponse> getProjects() {
        return projectRepo.findAll().stream().map(ProjectResponse::from).toList();
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        Project project = new Project(request.name(), request.key(), request.description());
        project = projectRepo.save(project);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
        if (request.name() != null && !request.name().isBlank()) {
            project.setName(request.name());
        }
        if (request.key() != null && !request.key().isBlank()) {
            project.setKey(request.key());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        project = projectRepo.save(project);
        return ProjectResponse.from(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepo.existsById(id)) {
            throw new RuntimeException("Project not found: " + id);
        }
        projectRepo.deleteById(id);
    }
}
