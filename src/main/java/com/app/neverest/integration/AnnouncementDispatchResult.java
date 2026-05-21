package com.app.neverest.integration;

public record AnnouncementDispatchResult(
        AnnouncementChannel channel,
        boolean attempted,
        boolean success,
        Integer statusCode,
        String detail
) {
}
