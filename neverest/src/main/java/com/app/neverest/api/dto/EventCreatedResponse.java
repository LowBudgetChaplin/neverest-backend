package com.app.neverest.api.dto;

import java.util.List;

public record EventCreatedResponse(
        EventResponse event,
        List<AnnouncementDispatchResponse> announcements
) {
}
