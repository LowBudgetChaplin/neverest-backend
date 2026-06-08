package com.app.neverest.api;

import com.app.neverest.api.dto.MeResponse;
import com.app.neverest.api.dto.LoginRequest;
import com.app.neverest.api.dto.LoginResponse;
import com.app.neverest.api.dto.RegisterRequest;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.common.ConflictException;
import com.app.neverest.service.LocalAuthService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LocalAuthService localAuthService;

    public AuthController(LocalAuthService localAuthService) {
        this.localAuthService = localAuthService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        try {
            LocalAuthService.LoginResult loginResult = localAuthService.login(request.email(), request.password());
            return new LoginResponse(loginResult.accessToken(), "Bearer", loginResult.expiresInSeconds());
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
    }

    @PostMapping("/register")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        try {
            localAuthService.register(
                    request.email(),
                    request.password(),
                    request.displayName(),
                    request.phoneNumber(),
                    request.avatarB64()
            );
        } catch (ConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        if (authentication == null) {
            return new MeResponse("anonymous", false, List.of());
        }

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new MeResponse(
                authentication.getName(),
                authentication.isAuthenticated(),
                authorities
        );
    }
}
