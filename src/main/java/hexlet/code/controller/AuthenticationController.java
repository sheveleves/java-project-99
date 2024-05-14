package hexlet.code.controller;

import hexlet.code.dto.AuthRequest;
import hexlet.code.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping(path = "/api/login")
    public String createAuthenticationByToken(@RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()));
        return jwtUtils.generateToken(authRequest.getUsername());
    }
}
