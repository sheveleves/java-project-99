package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mappers.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusService {
    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAllTaskStatuses() {
        List<TaskStatus> taskStatuses = taskStatusRepository.findAll();
        return taskStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO getTaskStatusDTOById(Long id) {
        TaskStatus taskStatus = getTaskStatusById(id);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatus getTaskStatusById(Long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with ID = " + id + " not found."));
    }

    public TaskStatusDTO createTaskStatus(TaskStatusCreateDTO taskStatusCreateDTO) {
        TaskStatus taskStatus = taskStatusMapper.map(taskStatusCreateDTO);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO updateTaskStatus(TaskStatusUpdateDTO taskStatusUpdateDTO, Long id) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with ID = " + id + " not found."));
        taskStatusMapper.map(taskStatusUpdateDTO, taskStatus);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public void deleteTaskStatus(Long id) {
        taskStatusRepository.deleteById(id);
    }
}
