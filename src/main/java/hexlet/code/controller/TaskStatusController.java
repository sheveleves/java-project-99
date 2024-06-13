package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.TaskStatusDeletingException;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.service.TaskStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task_statuses")
@AllArgsConstructor
@Tag(name = "Task Status management")
public class TaskStatusController {
    private final TaskStatusService taskStatusService;

    @GetMapping(path = "")
    public ResponseEntity<List<TaskStatusDTO>> showAllUsers() {
        List<TaskStatusDTO> taskStatuses = taskStatusService.getAllTaskStatuses();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(taskStatuses.size()))
                .body(taskStatuses);
    }

    @GetMapping(path = "/{id}")
    TaskStatusDTO getTaskStatusById(@PathVariable Long id) {
        return taskStatusService.getTaskStatusDTOById(id);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO createTaskStatus(@Valid @RequestBody TaskStatusCreateDTO taskStatusCreateDTO) {
        return taskStatusService.createTaskStatus(taskStatusCreateDTO);
    }

    @PutMapping(path = "/{id}")
    public TaskStatusDTO updateTaskStatus(@Valid @RequestBody TaskStatusUpdateDTO taskStatusUpdateDTO,
                                          @PathVariable Long id) {
        return taskStatusService.updateTaskStatus(taskStatusUpdateDTO, id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskStatus(@PathVariable Long id) {
        TaskStatus taskStatus = taskStatusService.getTaskStatusById(id);
        List<Task> tasks = taskStatus.getTasks();
        if (!tasks.isEmpty()) {
            throw new TaskStatusDeletingException(String.format("Can't delete the status with "
                    + "ID = %d because this status is used in the task(s)!", id));
        }
        taskStatusService.deleteTaskStatus(id);
    }
}
