package self.project.web.ticket.service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import self.project.web.ticket.service.dto.*;
import self.project.web.ticket.service.service.CommonService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CommonController {

    private final CommonService commonService;

    @GetMapping("/projects")
    public List<ProjectResponse> getProjects() {
        log.info("[GET /projects] Got request!");
        var result = commonService.getProjects();
        log.info("[GET /projects] Sending response");
        return result;
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        log.info("[GET /users] Got request!");
        var result = commonService.getUsers();
        log.info("[GET /users] Sending response");
        return result;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        log.info("[GET /me] Got request! User = {}", principal.getUsername());
        var result = commonService.getUserByUsername(principal.getUsername());
        log.info("[GET /me] Sending response: {}", result);
        return result;
    }
}