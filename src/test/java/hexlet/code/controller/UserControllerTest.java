package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
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
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext wac;
    private User testUser;
    private JwtRequestPostProcessor token;
    private TaskStatus testTaskStatus;
    private Task testTask;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        userRepository.save(testUser);
    }

    @Test
    public void testShowAllUsers() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/users")
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).isArray();
    }

    @Test
    public void testGetUserById() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/users/{id}", testUser.getId())
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("firstName").isEqualTo(testUser.getFirstName()),
                jsonAssert -> jsonAssert.node("lastName").isEqualTo(testUser.getLastName()),
                jsonAssert -> jsonAssert.node("email").isEqualTo(testUser.getEmail()));
    }

    @Test
    public void testCreateUser() throws Exception {
        long countBeforeCreateUser = userRepository.count();
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test@email.com");
        userCreateDTO.setPassword("testPassword");
        userCreateDTO.setFirstName("testFirstName");
        userCreateDTO.setLastName("testLastName");
        MockHttpServletRequestBuilder request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDTO));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        long countAfterCreateUser = userRepository.count();
        assertThat(countAfterCreateUser - countBeforeCreateUser).isEqualTo(1);
        User user = userRepository.findByEmail(userCreateDTO.getEmail()).get();
        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(userCreateDTO.getFirstName());
        assertThat(user.getLastName()).isEqualTo(userCreateDTO.getLastName());
    }

    @Test
    public void testUpdateUser() throws Exception {
        String oldUserLastName = testUser.getLastName();
        HashMap<String, String> data = new HashMap<>();
        data.put("firstName", "NewName");
        MockHttpServletRequestBuilder request = put("/api/users/{id}", testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isOk());
        User user = userRepository.findById(testUser.getId()).get();
        assertThat(user.getFirstName()).isEqualTo("NewName");
        assertThat(user.getLastName()).isEqualTo(oldUserLastName);
    }

    @Test
    public void testDeleteUser() throws Exception {
        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(true);
        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser.getId())
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(false);
    }

    @Test
    public void testBadEmailRequestForCreateUser() throws Exception {
        long countBeforeCreateUser = userRepository.count();
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test");
        userCreateDTO.setPassword("testPassword");
        MockHttpServletRequestBuilder request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDTO));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
        long countAfterCreateUser = userRepository.count();
        assertThat(countAfterCreateUser - countBeforeCreateUser).isEqualTo(0);
    }

    @Test
    public void testBadPasswordRequestForCreateUser() throws Exception {
        long countBeforeCreateUser = userRepository.count();
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("badPassword@gmail.com");
        userCreateDTO.setPassword("12");
        MockHttpServletRequestBuilder request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDTO));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
        long countAfterCreateUser = userRepository.count();
        assertThat(countAfterCreateUser - countBeforeCreateUser).isEqualTo(0);
    }

    @Test
    public void testNotFoundException() throws Exception {
        User user = userRepository.findTopByOrderByIdDesc();
        Long id = user.getId() + 1;
        MockHttpServletRequestBuilder request = get("/api/users/{id}", id)
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testDeleteUserWithTask() throws Exception {
        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(true);

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatus()).create();
        taskStatusRepository.save(testTaskStatus);

        testTask = Instancio.of(modelGenerator.getTask()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser.getId())
                .with(token);
        mockMvc.perform(request).andExpect(status().isConflict()).andReturn();
        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(true);
    }
}
