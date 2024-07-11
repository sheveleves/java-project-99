package hexlet.code.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.mappers.TaskMapper;
import hexlet.code.mappers.TaskStatusMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import jakarta.transaction.Transactional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
 .JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ModelGenerator modelGenerator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusMapper taskStatusMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LabelRepository labelRepository;

    private JwtRequestPostProcessor token;
    private User testUser;
    private TaskStatus testTaskStatus;
    private Task testTask;
    private Label testLabel;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatus()).create();
        taskStatusRepository.save(testTaskStatus);

        testLabel = Instancio.of(modelGenerator.getLabel()).create();
        labelRepository.save(testLabel);

        testTask = Instancio.of(modelGenerator.getTask()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setLabels(Set.of(testLabel));
        testTask.setAssignee(testUser);
        taskRepository.save(testTask);
    }

    @Test
    public void testShowAllTasks() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/tasks")
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).isArray();
    }

    @Test
    public void testTaskById() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/tasks/{id}", testTask.getId())
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(testTask.getId()),
                jsonAssert -> jsonAssert.node("index").isEqualTo(testTask.getIndex()),
                jsonAssert -> jsonAssert.node("title").isEqualTo(testTask.getName()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(testTask.getDescription()),
                jsonAssert -> jsonAssert.node("status").isEqualTo(testTask.getTaskStatus().getSlug()));
    }

    @Test()
    public void testCreateWrongTaskWithoutTaskStatus() throws Exception {
        long countBeforeCreateTask = taskRepository.count();
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("withoutTaskStatus");
        taskCreateDTO.setIndex(555);
        taskCreateDTO.setContent("This task doesn't have task status");
        MockHttpServletRequestBuilder request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDTO));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest()).andReturn();
        long countAfterCreateTask = taskRepository.count();
        assertThat(countAfterCreateTask - countBeforeCreateTask).isEqualTo(0);
    }


    @Test
    @Transactional
    public void testCreateTask() throws Exception {
        long countBeforeCreateTask = taskRepository.count();
        Label dataLabel = Instancio.of(modelGenerator.getLabel()).create();
        labelRepository.save(dataLabel);

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("testTitle");
        taskCreateDTO.setIndex(555);
        taskCreateDTO.setContent("testContent");
        taskCreateDTO.setStatus(testTaskStatus.getSlug());
        taskCreateDTO.setAssigneeId(testUser.getId());
        taskCreateDTO.setTaskLabelIds(Set.of(dataLabel.getId()));

        MockHttpServletRequestBuilder request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDTO));
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated()).andReturn();
        long countAfterCreateTask = taskRepository.count();
        assertThat(countAfterCreateTask - countBeforeCreateTask).isEqualTo(1);

        String content = result.getResponse().getContentAsString();

        Map<String, Object> map = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() { });

        String id = String.valueOf(map.get("id"));
        Task task = taskRepository.findById(Long.valueOf(id)).get();

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(taskCreateDTO.getTitle());
        assertThat(task.getDescription()).isEqualTo(taskCreateDTO.getContent());
        assertThat(task.getLabels()).isEqualTo(Set.of(dataLabel));
        assertThat(task.getAssignee()).isEqualTo(testUser);
    }


    @Test
    public void testUpdateTask() throws Exception {
        String oldName = testTask.getName();
        Task task = taskRepository.findTaskWithLabels(testTask.getId());
        Set<Label> oldLabels = task.getLabels();
        Label label = (Label) oldLabels.toArray()[0];
        Label newLabel = new Label();
        newLabel.setName(label.getName() + "newLabelName");
        labelRepository.save(newLabel);
        Set<Long> newSetLabels = Set.of(label.getId(), newLabel.getId());
        HashMap<String, Object> data = new HashMap<>();
        data.put("index", "555");
        data.put("content", "NewDescription");
        data.put("taskLabelIds", newSetLabels);
        MockHttpServletRequestBuilder request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isOk());
        testTask = taskRepository.findTaskWithLabels(testTask.getId());
        assertThat(testTask.getName()).isEqualTo(oldName);
        assertThat(testTask.getDescription()).isEqualTo("NewDescription");
        assertThat(testTask.getIndex()).isEqualTo(555);
        assertThat(testTask.getLabels().containsAll(Set.of(label, newLabel))).isEqualTo(true);
    }

    @Test
    public void testDeleteTask() throws Exception {
        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(true);
        MockHttpServletRequestBuilder request = delete("/api/tasks/{id}", testTask.getId())
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(false);
    }

    @Test
    public void testNotFoundException() throws Exception {
        Task task = taskRepository.findTopByOrderByIdDesc();
        Long id = task.getId() + 1;
        MockHttpServletRequestBuilder request = get("/api/tasks/{id}", id)
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testTaskFilter() throws Exception {
        TaskStatus taskStatus = Instancio.of(modelGenerator.getTaskStatus()).create();
        taskStatusRepository.save(taskStatus);

        Task task = Instancio.of(modelGenerator.getTask()).create();
        task.setTaskStatus(taskStatus);
        task.setLabels(Set.of(testLabel));
        taskRepository.save(task);

        MockHttpServletRequestBuilder request = get("/api/tasks")
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        assertThatJson(content)
                .inPath("$[*].title")
                .isArray()
                .contains(task.getName(), testTask.getName());

        assertThatJson(content)
                .inPath("$[*].assignee_id")
                .isArray()
                .contains(testUser.getId());

        assertThatJson(content)
                .inPath("$[*].status")
                .isArray()
                .contains(testTaskStatus.getSlug(), taskStatus.getSlug());

        assertThatJson(content)
                .inPath("$[*].taskLabelIds")
                .isArray()
                .contains(List.of(testLabel.getId()));

        request = get("/api/tasks?"
                + "labelId=" + testLabel.getId())
                .with(token);
        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        content = result.getResponse().getContentAsString();

        assertThatJson(content)
                .inPath("$[*].taskLabelIds")
                .isArray()
                .containsOnly(List.of(testLabel.getId()));

        request = get("/api/tasks?"
                + "labelId=" + testLabel.getId()
                + "&assigneeId=" + testUser.getId())
                .with(token);
        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        content = result.getResponse().getContentAsString();

        assertThatJson(content)
                .inPath("$[*].assignee_id")
                .isArray()
                .containsOnly(testUser.getId());

        request = get("/api/tasks?"
                + "labelId=" + testLabel.getId()
                + "&status=" + taskStatus.getSlug())
                .with(token);
        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        content = result.getResponse().getContentAsString();

        assertThatJson(content)
                .inPath("$[*].status")
                .isArray()
                .containsOnly(taskStatus.getSlug());
    }
}
