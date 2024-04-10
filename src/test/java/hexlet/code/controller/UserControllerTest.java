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

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
    }

    @Test
    public void testShowAllUsers() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/users");
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertThatJson(content).isArray();
    }

    @Test
    public void testGetUserById() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/users/{id}", testUser.getId());
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
        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(false);
    }
}
