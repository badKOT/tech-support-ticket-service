package self.project.web.ticket.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import self.project.web.ticket.service.dto.LoginResponse;
import self.project.web.ticket.service.dto.UserResponse;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.security.JwtService;
import self.project.web.ticket.service.security.RefreshTokenService;

@RestController
@RequestMapping("/api/auth/oidc")
@RequiredArgsConstructor
public class OidcAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/token")
    public LoginResponse exchangeOidcSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OIDC session not found");
        }

        Object emailAttribute = session.getAttribute("OIDC_EMAIL");

        if (!(emailAttribute instanceof String email) || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "OIDC authentication not found");
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            System.out.println("OIDC TOKEN: local user not found for email=" + email);

            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Local user not found");
        });

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is disabled");
        }

        String accessToken = jwtService.createAccessToken(user);

        String refreshToken = refreshTokenService.create(user).token();

        LoginResponse response = new LoginResponse(accessToken, refreshToken, "Bearer",
            UserResponse.from(user));

        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
        }

        return response;
    }
}