package self.project.web.ticket.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import self.project.web.ticket.service.dto.LoginRequest;
import self.project.web.ticket.service.dto.UserResponse;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public UserResponse login(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Authentication authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.username(),
                loginRequest.password()
            );

        Authentication authentication =
            authenticationManager.authenticate(authenticationRequest);

        sessionAuthenticationStrategy.onAuthentication(
            authentication,
            request,
            response
        );

        SecurityContext securityContext =
            SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(
            securityContext,
            request,
            response
        );

        return findUser(authentication.getName());
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
        Authentication authentication
    ) {
        return findUser(authentication.getName());
    }

    @GetMapping("/csrf")
    public CsrfToken getCsrfToken(CsrfToken csrfToken) {
        return csrfToken;
    }

    private UserResponse findUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "User not found: " + username
                )
            );

        return UserResponse.from(user);
    }
}
