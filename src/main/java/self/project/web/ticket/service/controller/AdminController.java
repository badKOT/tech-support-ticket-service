package self.project.web.ticket.service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        log.info("[GET /admin/users] Got request!");
        var result = adminService.getUsers();
        log.info("[GET /admin/users] Sending response");
        return result;
    }

    @PostMapping("/users")
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        log.info("[POST /admin/users] Got request! Body = {}", request);
        var result = adminService.createUser(request);
        log.info("[POST /admin/users] Sending response: {}", result);
        return result;
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        log.info("[PUT /admin/users/{}] Got request! Body = {}", id, request);
        var result = adminService.updateUser(id, request);
        log.info("[PUT /admin/users/{}] Sending response: {}", id, result);
        return result;
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("[DELETE /admin/users/{}] Got request!", id);
        adminService.deleteUser(id);
        log.info("[DELETE /admin/users/{}] Done", id);
    }

    @GetMapping("/projects")
    public List<ProjectResponse> getProjects() {
        log.info("[GET /admin/projects] Got request!");
        var result = adminService.getProjects();
        log.info("[GET /admin/projects] Sending response");
        return result;
    }

    @PostMapping("/projects")
    public ProjectResponse createProject(@RequestBody ProjectCreateRequest request) {
        log.info("[POST /admin/projects] Got request! Body = {}", request);
        var result = adminService.createProject(request);
        log.info("[POST /admin/projects] Sending response: {}", result);
        return result;
    }

    @PutMapping("/projects/{id}")
    public ProjectResponse updateProject(@PathVariable Long id, @RequestBody ProjectUpdateRequest request) {
        log.info("[PUT /admin/projects/{}] Got request! Body = {}", id, request);
        var result = adminService.updateProject(id, request);
        log.info("[PUT /admin/projects/{}] Sending response: {}", id, result);
        return result;
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable Long id) {
        log.info("[DELETE /admin/projects/{}] Got request!", id);
        adminService.deleteProject(id);
        log.info("[DELETE /admin/projects/{}] Done", id);
    }
}
