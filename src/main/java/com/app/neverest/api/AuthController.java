package com.app.neverest.api;

import com.app.neverest.api.dto.MeResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

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
