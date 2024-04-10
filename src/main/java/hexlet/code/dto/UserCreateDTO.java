package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCreateDTO {
    @Email(message = "Email must be in email format")
    @NotBlank(message = "Email must not be empty")
    private String email;
    private String firstName;
    private String lastName;
    @Size(min = 3, message = "Password must contain at least three characters")
    @NotNull(message = "Password must not be null")
    private String password;
}
