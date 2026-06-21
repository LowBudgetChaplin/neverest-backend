package com.app.neverest.api;

import com.app.neverest.api.dto.NotificationResponse;
import com.app.neverest.common.AuthUtils;
import com.app.neverest.persistence.entity.NotificationEntity;
import com.app.neverest.service.NeverestCoreService;
import com.app.neverest.service.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NeverestCoreService coreService;

    public NotificationController(NotificationService notificationService, NeverestCoreService coreService) {
        this.notificationService = notificationService;
        this.coreService = coreService;
    }

    @GetMapping("/me")
    public List<NotificationResponse> myNotifications(Authentication authentication) {
        UUID userId = coreService.resolveUserIdForAction(AuthUtils.requireSubject(authentication), null);
        return notificationService.list(userId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/me/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        UUID userId = coreService.resolveUserIdForAction(AuthUtils.requireSubject(authentication), null);
        return Map.of("count", notificationService.unreadCount(userId));
    }

    @PostMapping("/me/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(Authentication authentication) {
        UUID userId = coreService.resolveUserIdForAction(AuthUtils.requireSubject(authentication), null);
        notificationService.markAllRead(userId);
    }

    private NotificationResponse toResponse(NotificationEntity n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.isRead(),
                n.getChallengeId(),
                n.getSubmissionId(),
                n.getCreatedAt()
        );
    }
}
