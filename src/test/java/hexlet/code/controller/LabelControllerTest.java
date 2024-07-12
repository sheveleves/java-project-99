package hexlet.code.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {
    @Autowired
    private ModelGenerator modelGenerator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private WebApplicationContext wac;
    private User testUser;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private Label testLabel;


    @BeforeEach
    public void setUp() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testLabel = Instancio.of(modelGenerator.getLabel()).create();
        labelRepository.save(testLabel);
    }

    @Test
    public void testShowAllLabels() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/labels")
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).isArray();
    }

    @Test
    public void testCreatingMethodToFindLabelByName() throws Exception {
        assertThat(labelRepository.findByName(testLabel.getName()).isPresent()).isTrue();
    }

    @Test
    public void testLabelById() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/labels/{id}", testLabel.getId())
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(testLabel.getId()),
                jsonAssert -> jsonAssert.node("name").isEqualTo(testLabel.getName()));
    }

    @Test
    public void testCreateLabel() throws Exception {
        long countBeforeCreateLabel = labelRepository.count();
        LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName("testLabel");
        MockHttpServletRequestBuilder request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelCreateDTO));
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated()).andReturn();
        long countAfterCreateTask = labelRepository.count();
        assertThat(countAfterCreateTask - countBeforeCreateLabel).isEqualTo(1);

        String content = result.getResponse().getContentAsString();
        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("name").isEqualTo(labelCreateDTO.getName()));

        Label label = labelRepository.findByName(labelCreateDTO.getName()).get();

        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(labelCreateDTO.getName());
    }

    @Test
    public void testUpdateLabel() throws Exception {
        String oldName = testLabel.getName();
        HashMap<String, String> data = new HashMap<>();
        data.put("name", oldName + "newName");
        MockHttpServletRequestBuilder request = put("/api/labels/{id}", testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isOk());
        testLabel = labelRepository.findById(testLabel.getId()).get();
        assertThat(testLabel.getName()).isEqualTo(oldName + "newName");
    }

    @Test
    public void testDeleteLabel() throws Exception {
        assertThat(labelRepository.existsById(testLabel.getId())).isEqualTo(true);
        MockHttpServletRequestBuilder request = delete("/api/labels/{id}", testLabel.getId())
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertThat(labelRepository.existsById(testLabel.getId())).isEqualTo(false);
    }

    @Test
    public void testNotFoundException() throws Exception {
        Label label = labelRepository.findTopByOrderByIdDesc();
        Long id = label.getId() + 1;
        MockHttpServletRequestBuilder request = get("/api/labels/{id}", id)
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testDeletingLabelUsedInTheTasks() throws Exception {
        TaskStatus dataTaskStatus = Instancio.of(modelGenerator.getTaskStatus()).create();
        taskStatusRepository.save(dataTaskStatus);

        Task dataTask = Instancio.of(modelGenerator.getTask()).create();
        dataTask.setLabels(Set.of(testLabel));
        dataTask.setTaskStatus(dataTaskStatus);
        taskRepository.save(dataTask);

        Long id = testLabel.getId();
        MockHttpServletRequestBuilder request = delete("/api/labels/{id}", id)
                .with(token);
        mockMvc.perform(request).andExpect(status().isConflict()).andReturn();
        assertThat(labelRepository.findById(id).isPresent()).isTrue();
    }
}
