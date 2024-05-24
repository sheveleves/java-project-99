package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusCreateDTO {
    @NotBlank(message = "The name must contain at least one character")
    private String name;
    @NotBlank(message = "The name must contain at least one character")
    private String slug;
}
