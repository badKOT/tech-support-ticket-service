package self.project.web.ticket.service.service;

import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final PasswordService passwordService;

    public List<UserResponse> getUsers() {
        return userRepo.findAll()
            .stream()
            .map(UserResponse::from)
            .toList();
    }

    @Transactional
    public UserResponse createUser(
        UserCreateRequest request
    ) {
        PasswordService.PasswordData passwordData =
            passwordService.encode(request.password());

        User user = new User(
            request.username(),
            request.displayName(),
            request.email(),
            passwordData.passwordHash(),
            passwordData.salt(),
            request.role()
        );

        user = userRepo.save(user);

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(
        Long id,
        UserUpdateRequest request
    ) {
        User user = getUser(id);

        if (request.username() != null) {
            String username = request.username().trim();

            if (username.isBlank()) {
                throw new IllegalArgumentException(
                    "Username must not be blank"
                );
            }

            if (userRepo.existsByUsernameAndIdNot(username, id)) {
                throw new IllegalArgumentException(
                    "Username is already occupied: " + username
                );
            }

            user.setUsername(username);
        }

        if (request.displayName() != null) {
            String displayName = request.displayName().trim();

            if (displayName.isBlank()) {
                throw new IllegalArgumentException(
                    "Display name must not be blank"
                );
            }

            user.setDisplayName(displayName);
        }

        if (request.email() != null) {
            user.setEmail(request.email());
        }

        if (request.password() != null
            && !request.password().isBlank()) {

            PasswordService.PasswordData passwordData =
                passwordService.encode(request.password());

            user.setPasswordHash(
                passwordData.passwordHash()
            );

            user.setSalt(
                passwordData.salt()
            );
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        userRepo.delete(user);
    }

    public List<ProjectResponse> getProjects() {
        return projectRepo.findAll()
            .stream()
            .map(ProjectResponse::from)
            .toList();
    }

    @Transactional
    public ProjectResponse createProject(
        ProjectCreateRequest request
    ) {
        Project project = new Project(
            request.name(),
            request.key(),
            request.description()
        );

        project = projectRepo.save(project);

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(
        Long id,
        ProjectUpdateRequest request
    ) {
        Project project = projectRepo.findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "Project not found: " + id
                )
            );

        if (request.name() != null
            && !request.name().isBlank()) {

            project.setName(request.name());
        }

        if (request.key() != null
            && !request.key().isBlank()) {

            project.setKey(request.key());
        }

        if (request.description() != null) {
            project.setDescription(
                request.description()
            );
        }

        project = projectRepo.save(project);

        return ProjectResponse.from(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepo.existsById(id)) {
            throw new RuntimeException(
                "Project not found: " + id
            );
        }

        projectRepo.deleteById(id);
    }

    private User getUser(Long id) {
        return userRepo.findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "User not found: " + id
                )
            );
    }
}

