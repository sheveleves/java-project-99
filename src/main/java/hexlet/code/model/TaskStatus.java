package hexlet.code.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "taskStatus")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@ToString
@NoArgsConstructor
public class TaskStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Include
    @NotBlank(message = "The name must contain at least one character")
    private String name;

    @ToString.Include
    @Column(unique = true)
    @NotBlank(message = "The name must contain at least one character")
    private String slug;

    @CreatedDate
    private Instant createdAt;

    @OneToMany(mappedBy = "taskStatus")
    private List<Task> tasks;

    public TaskStatus(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
