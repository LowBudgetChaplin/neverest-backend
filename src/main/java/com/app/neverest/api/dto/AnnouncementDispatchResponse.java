package com.app.neverest.api.dto;

import com.app.neverest.integration.AnnouncementChannel;

public record AnnouncementDispatchResponse(
        AnnouncementChannel channel,
        boolean attempted,
        boolean success,
        Integer statusCode,
        String detail
) {
}
