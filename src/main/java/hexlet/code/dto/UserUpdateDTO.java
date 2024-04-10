package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class UserUpdateDTO {
    private JsonNullable<String> firstName;
    private JsonNullable<String> lastName;
    @Email(message = "Email must be in email format")
    @NotBlank(message = "Email must not be empty")
    private JsonNullable<String> email;
    @Size(min = 3, message = "Password must contain at least three characters")
    @NotNull(message = "Password must not be null")
    private JsonNullable<String> password;
}
