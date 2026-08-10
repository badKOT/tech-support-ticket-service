package self.project.web.ticket.service.service;

import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import self.project.web.ticket.service.security.PepperedPasswordEncoder;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PepperedPasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordData encode(String rawPassword) {

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                "Password must not be empty"
            );
        }

        String salt = generateSalt();

        String passwordHash =
            passwordEncoder.encode(
                rawPassword,
                salt
            );

        return new PasswordData(
            passwordHash,
            salt
        );
    }

    private String generateSalt() {
        byte[] salt = new byte[16];

        secureRandom.nextBytes(salt);

        return Base64.getEncoder()
            .encodeToString(salt);
    }

    public record PasswordData(
        String passwordHash,
        String salt
    ) {
    }
}
