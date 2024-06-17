package hexlet.code.specification;

import hexlet.code.dto.TaskParamDTO;
import hexlet.code.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(TaskParamDTO taskParamDTO) {
        return withTitleCont(taskParamDTO.getTitleCont())
                .and(withAssigneeId(taskParamDTO.getAssigneeId()))
                .and(withStatus(taskParamDTO.getStatus()))
                .and(withLabel(taskParamDTO.getLabelId()));
    }

    private Specification<Task> withTitleCont(String titleCont) {
        return (root, query, criteriaBuilder) -> titleCont == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(root.get("name"), "%" + titleCont + "%");
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, criteriaBuilder) ->
                assigneeId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId);
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("taskStatus").get("slug"), status);
    }

    private Specification<Task> withLabel(Long labelId) {
        return (root, query, criteriaBuilder) ->
                labelId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.join("labels").get("id"), labelId);
    }
}
