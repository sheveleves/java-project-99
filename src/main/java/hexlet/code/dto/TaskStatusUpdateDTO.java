package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @NotBlank(message = "The name must contain at least one character")
    private JsonNullable<String> name;
    @NotBlank(message = "The name must contain at least one character")
    private JsonNullable<String> slug;
}
