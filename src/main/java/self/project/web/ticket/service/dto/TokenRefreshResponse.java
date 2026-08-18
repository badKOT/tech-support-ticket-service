package self.project.web.ticket.service.dto;

public record TokenRefreshResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {
}