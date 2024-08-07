package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final Faker faker;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";
        User user = new User();
        String encodedPassword = passwordEncoder.encode("qwerty");
        if (userRepository.findByEmail(email).isEmpty()) {
            user.setEmail(email);
            user.setPasswordDigest(encodedPassword);
            userRepository.save(user);
        }

        Map<String, String> taskStatuses = Map.of(
                "Draft", "draft",
                "ToReview", "to_review",
                "ToBeFixed", "to_be_fixed",
                "ToPublish", "to_publish",
                "Published", "published"
        );

        taskStatuses.forEach((name, slug) -> {
            if (taskStatusRepository.findBySlug(slug).isEmpty()) {
                TaskStatus taskStatus = new TaskStatus(name, slug);
                taskStatusRepository.save(taskStatus);
            }
        });

        List.of("feature", "bug").stream().forEach((name) -> {
            if (labelRepository.findByName(name).isEmpty()) {
                labelRepository.save(new Label(name));
            }
        });
    }
}
