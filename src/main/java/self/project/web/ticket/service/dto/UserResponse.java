package self.project.web.ticket.service.dto;

import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.entity.UserRole;

public record UserResponse(
    Long id,
    String username,
    String displayName,
    String email,
    UserRole role,
    boolean enabled) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getRole(),
            user.isEnabled()
        );
    }
}