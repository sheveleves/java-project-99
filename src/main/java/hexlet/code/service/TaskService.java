package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mappers.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskMapper taskMapper;

    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID = " + id + " not found."));
        return taskMapper.map(task);
    }

    public TaskDTO createTask(TaskCreateDTO taskCreateDTO) {
        Task task = taskMapper.map(taskCreateDTO);
        taskRepository.save(task);
        taskMapper.map(task);
        return taskMapper.map(task);
    }

    public TaskDTO updateTask(TaskUpdateDTO taskUpdateDTO, Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID = " + id + " not found."));
        taskMapper.update(taskUpdateDTO, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
