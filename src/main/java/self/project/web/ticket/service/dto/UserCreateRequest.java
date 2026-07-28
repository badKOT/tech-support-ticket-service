package self.project.web.ticket.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import self.project.web.ticket.service.entity.UserRole;

public record UserCreateRequest(
    @NotBlank
    @Size(max = 100)
    String username,

    @NotBlank
    @Size(max = 150)
    String displayName,

    @Email
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(min = 8, max = 72)
    String password,

    @NotNull
    UserRole role
) {

}