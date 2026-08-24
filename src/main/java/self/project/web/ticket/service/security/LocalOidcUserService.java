package self.project.web.ticket.service.security;

import java.util.HashSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.stereotype.Service;

import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LocalOidcUserService {

    private final UserRepository userRepository;

    private final OidcUserService delegate = new OidcUserService();

    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getEmail();

        if (email == null || email.isBlank()) {
            throw oidcException("oidc_email_missing", "OIDC provider did not return email");
        }

        User localUser = userRepository.findByEmailIgnoreCase(email).orElseThrow(
            () -> oidcException("local_user_not_found", "No local user for OIDC email"));

        if (!localUser.isEnabled()) {
            throw oidcException("local_user_disabled", "Local user is disabled");
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + localUser.getRole().name()));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private OAuth2AuthenticationException oidcException(String code, String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), message);
    }
}