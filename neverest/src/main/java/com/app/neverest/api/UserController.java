package com.app.neverest.api;

import com.app.neverest.api.dto.CreateUserRequest;
import com.app.neverest.api.dto.UpdateProfileRequest;
import com.app.neverest.api.dto.UserResponse;
import com.app.neverest.audit.AuditLogService;
import com.app.neverest.common.AuthUtils;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.domain.UserProfile;
import com.app.neverest.service.NeverestCoreService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final NeverestCoreService coreService;
    private final AuditLogService auditLogService;

    public UserController(NeverestCoreService coreService, AuditLogService auditLogService) {
        this.coreService = coreService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        UserProfile userProfile = coreService.createUser(request.displayName());
        auditLogService.log(
                "USER_CREATED",
                AuthUtils.actor(authentication),
                true,
                "User profile created.",
                Map.of("userId", userProfile.id().toString())
        );
        return toResponse(userProfile);
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createMyUser(@RequestBody CreateUserRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        String authSubject = AuthUtils.requireSubject(authentication);
        UserProfile userProfile = coreService.createUser(request.displayName(), authSubject);
        auditLogService.log(
                "USER_CREATED",
                authSubject,
                true,
                "User profile created from /me endpoint.",
                Map.of("userId", userProfile.id().toString())
        );
        return toResponse(userProfile);
    }

    @PatchMapping("/me")
    public UserResponse updateMyUser(@RequestBody UpdateProfileRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        String authSubject = AuthUtils.requireSubject(authentication);
        UserProfile userProfile = coreService.updateMyProfile(
                authSubject,
                request.displayName(),
                request.phoneNumber(),
                request.avatarB64()
        );
        auditLogService.log(
                "USER_PROFILE_UPDATED",
                authSubject,
                true,
                "User profile updated.",
                Map.of("userId", userProfile.id().toString())
        );
        return toResponse(userProfile);
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return coreService.getUsers()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/me")
    public UserResponse getMyUser(Authentication authentication) {
        String authSubject = AuthUtils.requireSubject(authentication);
        return toResponse(coreService.getUserByAuthSubject(authSubject));
    }

    private UserResponse toResponse(UserProfile userProfile) {
        return new UserResponse(
                userProfile.id(),
                userProfile.displayName(),
                userProfile.qrCode(),
                userProfile.authSubject(),
                userProfile.totalPoints(),
                userProfile.availablePoints(),
                userProfile.pointsByActivity(),
                userProfile.phoneNumber(),
                userProfile.avatarB64()
        );
    }
}
