package com.app.neverest.api;

import com.app.neverest.api.dto.LeaderboardEntryResponse;
import com.app.neverest.domain.ActivityType;
import com.app.neverest.service.NeverestCoreService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final NeverestCoreService coreService;

    public LeaderboardController(NeverestCoreService coreService) {
        this.coreService = coreService;
    }

    @GetMapping("/general")
    public List<LeaderboardEntryResponse> general(@RequestParam(required = false) Integer limit) {
        return coreService.getGeneralLeaderboard(limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/activity/{activityType}")
    public List<LeaderboardEntryResponse> byActivity(
            @PathVariable ActivityType activityType,
            @RequestParam(required = false) Integer limit
    ) {
        return coreService.getActivityLeaderboard(activityType, limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LeaderboardEntryResponse toResponse(NeverestCoreService.LeaderboardEntry leaderboardEntry) {
        return new LeaderboardEntryResponse(
                leaderboardEntry.userId(),
                leaderboardEntry.displayName(),
                leaderboardEntry.avatarB64(),
                leaderboardEntry.points()
        );
    }
}
