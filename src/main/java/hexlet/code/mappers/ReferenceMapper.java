package hexlet.code.mappers;

import hexlet.code.exception.NullTaskStatusException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.BaseEntity;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceMapper {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private LabelRepository labelRepository;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        if (id == null) {
            return null;
        }
        T t = entityManager.find(entityClass, id);
        if (t == null) {
            throw new ResourceNotFoundException(entityClass.getSimpleName() + " with " + id + " not found!");
        } else {
            return t;
        }
    }

    @Named("statusToTaskStatus")
    public TaskStatus statusToTaskStatus(String status) {
        if (status == null) {
            throw new NullTaskStatusException("It's forbidden to create or update a task without a status!");
        }
        return taskStatusRepository.findBySlug(status)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with slug = " + status + " not found."));
    }

    @Named("labelsIdToLabels")
    public Set<Label> labelsIdToLabels(Set<Long> taskLabelIds) {
        if (taskLabelIds == null) {
            return null;
        }
        Set<Label> labelSet = labelRepository.findByIdIn(taskLabelIds);
        Set<Long> labeldIds = labelSet.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
        List<Long> noPresentInDatabase = new ArrayList<>();
        taskLabelIds.forEach(l -> {
            if (!labeldIds.contains(l)) {
                noPresentInDatabase.add(l);
            }
        });

        if (!noPresentInDatabase.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Label(s) with Id(s) = %s"
                    + " not found. Task not created!", noPresentInDatabase));
        }
        return labelSet;
    }


    @Named("labelsToLabelsIds")
    public Set<Long> labelsToLabelsIds(Set<Label> labels) {
        if (labels == null) {
            return new HashSet<>();
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }
}
