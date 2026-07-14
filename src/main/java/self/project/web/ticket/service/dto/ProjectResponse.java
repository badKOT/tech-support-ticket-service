package self.project.web.ticket.service.dto;

import self.project.web.ticket.service.entity.Project;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String key,
        String description,
        Instant createdAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getDescription(),
                project.getCreatedAt());
    }
}