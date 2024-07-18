package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<String> title;
    private JsonNullable<Integer> index;
    private JsonNullable<String> content;
    @NotNull(message = "TaskStatus of task must not be null")
    private JsonNullable<String> status;
    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;
    private JsonNullable<Set<Long>> taskLabelIds;
}
