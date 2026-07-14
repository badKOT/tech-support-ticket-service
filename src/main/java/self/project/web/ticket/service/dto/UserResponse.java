package self.project.web.ticket.service.dto;

import self.project.web.ticket.service.entity.User;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String email) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail());
    }
}