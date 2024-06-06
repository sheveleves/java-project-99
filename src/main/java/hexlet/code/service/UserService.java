package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mappers.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO getUserDTOById(Long id) {
        User user = getUserById(id);
        return userMapper.map(user);
    }

    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        User user = userMapper.map(userCreateDTO);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO updateUser(UserUpdateDTO userUpdateDTO, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID = " + id + " not found."));
        userMapper.update(userUpdateDTO, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID = " + id + " not found."));
    }
}
