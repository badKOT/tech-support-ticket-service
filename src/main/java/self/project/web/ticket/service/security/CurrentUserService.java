package self.project.web.ticket.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = getAuthentication();

        return userRepository.findByUsername(authentication.getName())
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Authenticated user not found: "
                        + authentication.getName()
                )
            );
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUsername() {
        return getAuthentication().getName();
    }

    private Authentication getAuthentication() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication
            instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                "User is not authenticated"
            );
        }

        return authentication;
    }
}
