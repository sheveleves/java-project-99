package hexlet.code.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Welcome page")
public class WelcomeController {
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Spring";
    }

    @GetMapping("/testsentry")
    public void testSentry() {
        throw new RuntimeException("Sentry checking!");
    }
}
