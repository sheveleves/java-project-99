package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    @NotBlank(message = "Task title(name) must not be empty")
    private String title;
    private int index;
    private String content;
    @NotNull(message = "TaskStatus of task must not be null")
    private String status;
    private Long assigneeId;
    private Set<Long> labelsIds;
}
