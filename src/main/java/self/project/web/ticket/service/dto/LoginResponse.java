package self.project.web.ticket.service.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    UserResponse user
) {
}
