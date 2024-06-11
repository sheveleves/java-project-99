package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mappers.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelMapper labelMapper;


    public List<LabelDTO> getAllLabels() {
        List<Label> labels = labelRepository.findAll();
        return labels.stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO getLabelDTOById(Long id) {
        return labelMapper.map(getLabelById(id));
    }

    public Label getLabelById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with ID = " + id + " not found."));
    }

    public LabelDTO createLabel(LabelCreateDTO labelCreateDTO) {
        Label label = labelMapper.map(labelCreateDTO);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO updateLabel(LabelUpdateDTO labelUpdateDTO, Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with ID = " + id + " not found."));
        labelMapper.update(labelUpdateDTO, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void deleteLabel(Long id) {
        labelRepository.deleteById(id);
    }
}
