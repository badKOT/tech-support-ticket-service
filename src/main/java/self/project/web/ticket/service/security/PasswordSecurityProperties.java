package self.project.web.ticket.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordSecurityProperties {

    private final String pepper;

    public PasswordSecurityProperties(@Value("${security.password.pepper:test-pepper}") String pepper) {
        this.pepper = pepper;
    }

    public String getPepper() {
        return pepper;
    }
}
