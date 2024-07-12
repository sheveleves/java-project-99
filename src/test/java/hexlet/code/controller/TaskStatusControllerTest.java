package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
public class TaskStatusControllerTest {
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ModelGenerator modelGenerator;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext wac;
    private TaskStatus testTaskStatus;
    private JwtRequestPostProcessor token;
    private Task testTask;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatus()).create();
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        taskStatusRepository.save(testTaskStatus);
    }

    @Test
    public void testShowAllTaskStatuses() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/task_statuses")
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).isArray();
    }

    @Test
    public void testGetTaskStatusById() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("name").isEqualTo(testTaskStatus.getName()),
                jsonAssert -> jsonAssert.node("slug").isEqualTo(testTaskStatus.getSlug()));
    }

    @Test
    public void testCreateTaskStatus() throws Exception {
        long countBeforeCreateUser = taskStatusRepository.count();
        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName("testTaskStatus");
        taskStatusCreateDTO.setSlug("testSlug");
        MockHttpServletRequestBuilder request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatusCreateDTO));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        long countAfterCreateUser = taskStatusRepository.count();
        assertThat(countAfterCreateUser - countBeforeCreateUser).isEqualTo(1);
        TaskStatus taskStatus = taskStatusRepository.findBySlug(taskStatusCreateDTO.getSlug()).get();
        assertNotNull(taskStatus);
        assertThat(taskStatus.getName()).isEqualTo(taskStatusCreateDTO.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(taskStatusCreateDTO.getSlug());
    }

    @Test
    public void testBadNameRequestForCreateTaskStatus() throws Exception {
        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName("");
        MockHttpServletRequestBuilder request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatusCreateDTO));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        String testTaskStatusSlug = testTaskStatus.getSlug();
        HashMap<String, String> data = new HashMap<>();
        data.put("name", "NewName");
        MockHttpServletRequestBuilder request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isOk());
        TaskStatus taskStatus = taskStatusRepository.findById(testTaskStatus.getId()).get();
        assertThat(taskStatus.getName()).isEqualTo("NewName");
        assertThat(taskStatus.getSlug()).isEqualTo(testTaskStatusSlug);
    }

    @Test
    public void testDeleteTaskStatus() throws Exception {
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isEqualTo(true);
        MockHttpServletRequestBuilder request = delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isEqualTo(false);
    }

    @Test
    public void testDeleteTaskStatusWhichIsUsedIntoTask() throws Exception {
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isEqualTo(true);

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatus()).create();
        taskStatusRepository.save(testTaskStatus);

        testTask = Instancio.of(modelGenerator.getTask()).create();
        testTask.setTaskStatus(testTaskStatus);
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isConflict()).andReturn();
        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isEqualTo(true);
    }
}
