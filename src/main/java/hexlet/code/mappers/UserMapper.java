package hexlet.code.mappers;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;
    public abstract User map(UserCreateDTO userCreateDTO);
    public abstract UserDTO map(User user);
    public abstract void update(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    @BeforeMapping
    public void encryptPasswordUpdate(UserUpdateDTO update, @MappingTarget User user) {
        var password = update.getPassword();
        if (password != null && password.isPresent()) {
            user.setPassword(passwordEncoder.encode(password.get()));
        }
    }

    @BeforeMapping
    public void encryptPasswordCreate(UserCreateDTO data) {
        var password = data.getPassword();
        data.setPassword(passwordEncoder.encode(password));
    }
}
