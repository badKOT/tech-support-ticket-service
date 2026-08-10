package self.project.web.ticket.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PepperedPasswordEncoder {
    private final PasswordSecurityProperties properties;

    private final BCryptPasswordEncoder bcrypt =
        new BCryptPasswordEncoder();

    public String encode(String rawPassword, String salt) {
        return bcrypt.encode(
            rawPassword
                + properties.getPepper()
                + salt
        );
    }

    public boolean matches(
        String rawPassword,
        String salt,
        String passwordHash
    ) {
        return bcrypt.matches(
            rawPassword
                + properties.getPepper()
                + salt,
            passwordHash
        );
    }
}
