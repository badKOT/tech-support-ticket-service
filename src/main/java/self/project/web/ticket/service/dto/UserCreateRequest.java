package self.project.web.ticket.service.dto;

import self.project.web.ticket.service.entity.UserRole;

public record UserCreateRequest(
    String username,
    String displayName,
    String email,
    String password,
    UserRole role
) {
}