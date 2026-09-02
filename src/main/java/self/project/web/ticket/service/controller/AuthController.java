package self.project.web.ticket.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import self.project.web.ticket.service.dto.LoginRequest;
import self.project.web.ticket.service.dto.LoginResponse;
import self.project.web.ticket.service.dto.RefreshTokenRequest;
import self.project.web.ticket.service.dto.TokenRefreshResponse;
import self.project.web.ticket.service.dto.UserResponse;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.security.JwtService;
import self.project.web.ticket.service.security.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
            loginRequest.username(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        User user = findUserEntity(authentication.getName());

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = refreshTokenService.create(user).token();

        return new LoginResponse(accessToken, refreshToken, "Bearer", UserResponse.from(user));
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        User user = findUserEntity(authentication.getName());

        return UserResponse.from(user);
    }

    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(
            request.refreshToken());

        String accessToken = jwtService.createAccessToken(rotated.user());

        return new TokenRefreshResponse(accessToken, rotated.refreshToken(), "Bearer");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            refreshTokenService.revoke(request.refreshToken());
        }
    }

    private User findUserEntity(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}