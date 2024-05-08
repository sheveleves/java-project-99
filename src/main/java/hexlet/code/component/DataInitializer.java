package hexlet.code.component;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Faker faker;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";
        User user = new User();
        user.setEmail(email);
        String encodedPassword = passwordEncoder.encode("qwerty");
        user.setPasswordDigest(encodedPassword);
        userRepository.save(user);


        for (int i = 0; i++ < 10;) {
            user = new User();
            user.setEmail(faker.internet().emailAddress());
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            encodedPassword = passwordEncoder.encode("12345");
            user.setPasswordDigest(encodedPassword);
            userRepository.save(user);
        }
    }
}
