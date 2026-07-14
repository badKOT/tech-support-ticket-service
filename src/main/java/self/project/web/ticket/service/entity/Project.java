package self.project.web.ticket.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "projects")
@NoArgsConstructor
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, name = "project_key")
    private String key;

    @Column
    private String description;

    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    public Project(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
    }
}