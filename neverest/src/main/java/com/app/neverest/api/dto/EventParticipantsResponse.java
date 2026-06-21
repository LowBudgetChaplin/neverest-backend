package com.app.neverest.api.dto;

import java.util.List;

public record EventParticipantsResponse(
        boolean going,
        int count,
        List<EventParticipantResponse> participants
) {
}
