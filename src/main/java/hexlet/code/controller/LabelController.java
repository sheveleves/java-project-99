package hexlet.code.controller;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.LabelDeletingException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.service.LabelService;
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
import java.util.Set;

@RestController
@RequestMapping("/api/labels")
@AllArgsConstructor
public class LabelController {
    private final LabelService labelService;

    @GetMapping(path = "")
    public ResponseEntity<List<LabelDTO>> index() {
        List<LabelDTO> labels = labelService.getAllLabels();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @GetMapping(path = "/{id}")
    public LabelDTO getLabelDTOById(@PathVariable Long id) {
        return labelService.getLabelDTOById(id);
    }


    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody LabelCreateDTO labelCreateDTO) {
        return labelService.createLabel(labelCreateDTO);
    }

    @PutMapping(path = "/{id}")
    public LabelDTO updateLabel(@Valid @RequestBody LabelUpdateDTO labelUpdateDTO, @PathVariable Long id) {
        return labelService.updateLabel(labelUpdateDTO, id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable Long id) {
        Label label = labelService.getLabelById(id);
        Set<Task> tasks = label.getTasks();
        if (!tasks.isEmpty()) {
            throw new LabelDeletingException(String.format("Can't delete the label with "
                    + "ID = %d because this label is used in the task(s)!", id));
        }
        labelService.deleteLabel(id);
    }
}
