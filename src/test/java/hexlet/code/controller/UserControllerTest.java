package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mappers.UserMapper;
import hexlet.code.model.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
    private User testUser;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ObjectMapper objectMapper;
    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
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
        User data = Instancio.of(modelGenerator.getUserModel()).create();
        MockHttpServletRequestBuilder request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        long countAfterCreateUser = userRepository.count();
        assertThat(countAfterCreateUser - countBeforeCreateUser).isEqualTo(1);
        User user = userRepository.findByEmail(data.getEmail()).get();
        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(user.getLastName()).isEqualTo(data.getLastName());
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
        User data = Instancio.of(modelGenerator.getUserModel()).create();
        data.setEmail("test");
        MockHttpServletRequestBuilder request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("Email must be in email format");
    }

    @Test
    public void testBadPasswordRequestForCreateUser() throws Exception {
        User data = Instancio.of(modelGenerator.getUserModel()).create();
        data.setPasswordDigest("12");
        MockHttpServletRequestBuilder request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getContentAsString())
                .contains("Password must contain at least three characters");
    }

    @Test
    public void testNotFoundException() throws Exception {
        User user = userRepository.findTopByOrderByIdDesc();
        Long id = user.getId() + 1;
        MockHttpServletRequestBuilder request = get("/api/users/{id}", id)
                .with(token);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getContentAsString())
                .contains("User with ID = " + id + " not found.");
    }
}
