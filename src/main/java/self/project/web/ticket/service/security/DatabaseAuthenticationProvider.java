package self.project.web.ticket.service.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;
import org.springframework.security.core.AuthenticationException;

@Component
@RequiredArgsConstructor
public class DatabaseAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PepperedPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();

        String password =
            authentication.getCredentials().toString();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new BadCredentialsException(
                    "Invalid username or password"
                )
            );

        if (!user.isEnabled()) {
            throw new BadCredentialsException(
                "User is disabled"
            );
        }

        boolean passwordMatches =
            passwordEncoder.matches(
                password,
                user.getSalt(),
                user.getPasswordHash()
            );

        if (!passwordMatches) {
            throw new BadCredentialsException(
                "Invalid username or password"
            );
        }

        return new UsernamePasswordAuthenticationToken(
            user.getUsername(),
            null,
            List.of(
                new SimpleGrantedAuthority(
                    "ROLE_" + user.getRole().name()
                )
            )
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
            .isAssignableFrom(authentication);
    }
}
