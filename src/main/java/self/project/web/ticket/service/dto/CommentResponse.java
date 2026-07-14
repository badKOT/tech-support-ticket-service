package self.project.web.ticket.service.dto;

import self.project.web.ticket.service.entity.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorName,
        Instant createdAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getDisplayName(),
                comment.getCreatedAt());
    }
}