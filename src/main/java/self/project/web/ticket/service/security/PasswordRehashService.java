package self.project.web.ticket.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import self.project.web.ticket.service.entity.User;
import self.project.web.ticket.service.repository.UserRepository;
import self.project.web.ticket.service.service.PasswordService;

@Service
@RequiredArgsConstructor
public class PasswordRehashService {

    private final PasswordService passwordService;
    private final UserRepository userRepository;

    @Transactional
    public void rehash(User user, String rawPassword) {
        PasswordService.PasswordData passwordData = passwordService.encode(rawPassword);

        user.setPasswordHash(passwordData.passwordHash());

        user.setSalt(passwordData.salt());

        userRepository.save(user);
    }
}