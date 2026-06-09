package com.app.neverest.api;

import com.app.neverest.api.dto.AnnouncementDispatchResponse;
import com.app.neverest.api.dto.CheckInRequest;
import com.app.neverest.api.dto.CheckInResponse;
import com.app.neverest.api.dto.CreateEventRequest;
import com.app.neverest.api.dto.EventCreatedResponse;
import com.app.neverest.api.dto.EventResponse;
import com.app.neverest.audit.AuditLogService;
import com.app.neverest.common.AuthUtils;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.domain.Event;
import com.app.neverest.integration.AnnouncementDispatchResult;
import com.app.neverest.integration.AnnouncementTaskService;
import com.app.neverest.service.NeverestCoreService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final NeverestCoreService coreService;
    private final AnnouncementTaskService announcementTaskService;
    private final AuditLogService auditLogService;

    public EventController(
            NeverestCoreService coreService,
            AnnouncementTaskService announcementTaskService,
            AuditLogService auditLogService
    ) {
        this.coreService = coreService;
        this.announcementTaskService = announcementTaskService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventCreatedResponse createEvent(@RequestBody CreateEventRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        Event event = coreService.createEvent(
                request.title(),
                request.activityType(),
                request.location(),
                request.startsAt(),
                request.pointsReward(),
                request.capacity(),
                request.description(),
                request.recurrence(),
                request.routeMapUrl(),
                request.stravaClubUrl(),
                request.whatsappGroupUrl()
        );

        List<AnnouncementDispatchResult> announcementResults = announcementTaskService.dispatchOnEventCreated(event);
        String actor = AuthUtils.actor(authentication);
        auditLogService.log(
                "EVENT_CREATED",
                actor,
                true,
                "Event created successfully.",
                Map.of(
                        "eventId", event.id().toString(),
                        "title", event.title(),
                        "activityType", event.activityType().name()
                )
        );
        for (AnnouncementDispatchResult result : announcementResults) {
            auditLogService.log(
                    "EVENT_ANNOUNCEMENT_DISPATCH",
                    actor,
                    result.success(),
                    result.detail(),
                    Map.of(
                            "eventId", event.id().toString(),
                            "channel", result.channel().name(),
                            "attempted", String.valueOf(result.attempted()),
                            "statusCode", result.statusCode() == null ? "-" : String.valueOf(result.statusCode())
                    )
            );
        }

        return new EventCreatedResponse(
                toResponse(event),
                announcementResults.stream().map(this::toAnnouncementResponse).toList()
        );
    }

    @GetMapping
    public List<EventResponse> getEvents() {
        return coreService.getEvents()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{eventId}/check-ins")
    public CheckInResponse checkIn(
            @PathVariable UUID eventId,
            @RequestBody CheckInRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        NeverestCoreService.CheckInResult checkInResult = coreService.checkInToEvent(eventId, request.userQrCode());

        auditLogService.log(
                "EVENT_CHECK_IN",
                AuthUtils.actor(authentication),
                true,
                "User checked in to event.",
                Map.of(
                        "eventId", checkInResult.eventId().toString(),
                        "userId", checkInResult.userId().toString(),
                        "pointsAwarded", String.valueOf(checkInResult.pointsAwarded())
                )
        );

        return new CheckInResponse(
                checkInResult.eventId(),
                checkInResult.userId(),
                checkInResult.userName(),
                checkInResult.userAvatarB64(),
                checkInResult.pointsAwarded(),
                checkInResult.updatedTotalPoints(),
                checkInResult.checkInCount(),
                checkInResult.capacity()
        );
    }

    @PostMapping("/{eventId}/announcements/retry")
    public List<AnnouncementDispatchResponse> retryAnnouncements(
            @PathVariable UUID eventId,
            Authentication authentication
    ) {
        Event event = coreService.getEventById(eventId);
        List<AnnouncementDispatchResult> announcementResults = announcementTaskService.retryForEvent(event);
        String actor = AuthUtils.actor(authentication);
        for (AnnouncementDispatchResult result : announcementResults) {
            auditLogService.log(
                    "EVENT_ANNOUNCEMENT_RETRY",
                    actor,
                    result.success(),
                    result.detail(),
                    Map.of(
                            "eventId", event.id().toString(),
                            "channel", result.channel().name(),
                            "attempted", String.valueOf(result.attempted()),
                            "statusCode", result.statusCode() == null ? "-" : String.valueOf(result.statusCode())
                    )
            );
        }

        return announcementResults.stream().map(this::toAnnouncementResponse).toList();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.id(),
                event.title(),
                event.activityType(),
                event.location(),
                event.startsAt(),
                event.pointsReward(),
                event.capacity(),
                event.attendeeCount(),
                event.description(),
                event.recurrence(),
                event.routeMapUrl(),
                event.stravaClubUrl(),
                event.whatsappGroupUrl()
        );
    }

    private AnnouncementDispatchResponse toAnnouncementResponse(AnnouncementDispatchResult result) {
        return new AnnouncementDispatchResponse(
                result.channel(),
                result.attempted(),
                result.success(),
                result.statusCode(),
                result.detail()
        );
    }
}
