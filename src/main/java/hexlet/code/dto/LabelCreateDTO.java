package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelCreateDTO {
    @NotBlank(message = "The name must contain at least 3 and no more than 1000 characters")
    @Size(min = 3, max = 1000, message = "The name must contain at least 3 and no more than 1000 characters")
    private String name;
}
