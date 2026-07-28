package self.project.web.ticket.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import self.project.web.ticket.service.entity.UserRole;

public record UserUpdateRequest(

    @Size(max = 100)
    String username,

    @Size(max = 150)
    String displayName,

    @Email
    @Size(max = 255)
    String email,

    @Size(min = 8, max = 72)
    String password,

    UserRole role,

    Boolean enabled
) {

}