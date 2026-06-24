package com.app.neverest.api.dto;

import java.util.List;

public record EventParticipantsResponse(
        boolean going,
        int count,
        int checkedInCount,
        List<EventParticipantResponse> participants
) {
}
