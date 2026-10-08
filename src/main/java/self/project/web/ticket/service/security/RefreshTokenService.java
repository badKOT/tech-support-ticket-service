package self.project.web.ticket.service.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import self.project.web.ticket.service.config.JwtProperties;
import self.project.web.ticket.service.entity.RefreshToken;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public IssuedRefreshToken create(User user) {
        return createInternal(user);
    }

    @Transactional
    public RotatedRefreshToken rotate(String rawRefreshToken) {
        String tokenHash = hash(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
            .orElseThrow(RefreshTokenService::invalidRefreshToken);

        if (storedToken.isRevoked()) {
            throw invalidRefreshToken();
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.revoke();

            throw invalidRefreshToken();
        }

        User user = storedToken.getUser();

        if (!user.isEnabled()) {
            storedToken.revoke();

            throw invalidRefreshToken();
        }

        storedToken.revoke();

        IssuedRefreshToken newToken = createInternal(user);

        return new RotatedRefreshToken(user, newToken.token());
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = hash(rawRefreshToken);

        refreshTokenRepository.findByTokenHashForUpdate(tokenHash).ifPresent(RefreshToken::revoke);
    }

    private IssuedRefreshToken createInternal(User user) {
        byte[] bytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken(user, hash(rawToken),
            now.plus(jwtProperties.refreshTokenTtl()), now);

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(rawToken);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            "Invalid or expired refresh token");
    }

    public record IssuedRefreshToken(String token) {
    }

    public record RotatedRefreshToken(User user, String refreshToken) {
    }
}