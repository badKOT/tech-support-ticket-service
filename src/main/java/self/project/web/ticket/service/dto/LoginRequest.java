package self.project.web.ticket.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

    @NotBlank(message = "Username must not be blank")
    @Size(max = 100, message = "Username must contain no more than 100 characters")
    String username,

    @NotBlank(message = "Password must not be blank")
    @Size(max = 200, message = "Password must contain no more than 200 characters")
    String password
) {

}
