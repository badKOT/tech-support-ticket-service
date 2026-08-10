package self.project.web.ticket.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
    @NotBlank(message = "Comment must not be blank")
    @Size(max = 5000, message = "Comment must contain no more than 5000 characters")
    String content
) {

}