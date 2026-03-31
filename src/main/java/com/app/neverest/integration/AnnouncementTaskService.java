package com.app.neverest.integration;

import com.app.neverest.domain.Event;
import com.app.neverest.persistence.entity.AnnouncementTaskEntity;
import com.app.neverest.persistence.repository.AnnouncementTaskRepository;
import com.app.neverest.service.NeverestCoreService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementTaskService {

    private final AnnouncementTaskRepository announcementTaskRepository;
    private final EventAnnouncementService eventAnnouncementService;
    private final NeverestCoreService coreService;
    private final boolean retryEnabled;
    private final int retryBatchSize;
    private final int maxAttempts;
    private final int backoffSeconds;

    public AnnouncementTaskService(
            AnnouncementTaskRepository announcementTaskRepository,
            EventAnnouncementService eventAnnouncementService,
            NeverestCoreService coreService,
            @Value("${neverest.integrations.retry.enabled:true}") boolean retryEnabled,
            @Value("${neverest.integrations.retry.batch-size:25}") int retryBatchSize,
            @Value("${neverest.integrations.retry.max-attempts:5}") int maxAttempts,
            @Value("${neverest.integrations.retry.backoff-seconds:120}") int backoffSeconds
    ) {
        this.announcementTaskRepository = announcementTaskRepository;
        this.eventAnnouncementService = eventAnnouncementService;
        this.coreService = coreService;
        this.retryEnabled = retryEnabled;
        this.retryBatchSize = Math.max(1, retryBatchSize);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffSeconds = Math.max(1, backoffSeconds);
    }

    @Transactional
    public List<AnnouncementDispatchResult> dispatchOnEventCreated(Event event) {
        return dispatchForEvent(event, false);
    }

    @Transactional
    public List<AnnouncementDispatchResult> retryForEvent(Event event) {
        return dispatchForEvent(event, true);
    }

    @Scheduled(fixedDelayString = "${neverest.integrations.retry.fixed-delay-ms:60000}")
    @Transactional
    public void processRetryQueue() {
        if (!retryEnabled) {
            return;
        }

        List<AnnouncementTaskEntity> dueTasks = announcementTaskRepository.findDueTasks(
                List.of(AnnouncementTaskStatus.PENDING, AnnouncementTaskStatus.FAILED),
                LocalDateTime.now(),
                PageRequest.of(0, retryBatchSize)
        );

        for (AnnouncementTaskEntity task : dueTasks) {
            if (!task.canRetry() && task.getStatus() == AnnouncementTaskStatus.FAILED) {
                continue;
            }

            Event event;
            try {
                event = coreService.getEventById(task.getEventId());
            } catch (Exception exception) {
                task.markFailed(null, "Event not found for retry.", backoffSeconds);
                announcementTaskRepository.save(task);
                continue;
            }

            AnnouncementDispatchResult result = eventAnnouncementService.dispatchEventCreated(task.getChannel(), event);
            updateTaskAfterDispatch(task, result);
            announcementTaskRepository.save(task);
        }
    }

    private List<AnnouncementDispatchResult> dispatchForEvent(Event event, boolean manualRetry) {
        List<AnnouncementDispatchResult> results = new ArrayList<>();

        for (AnnouncementChannel channel : AnnouncementChannel.values()) {
            AnnouncementTaskEntity task = findOrCreateTask(event, channel);
            if (manualRetry) {
                task.resetForManualRetry();
            }

            AnnouncementDispatchResult result = eventAnnouncementService.dispatchEventCreated(channel, event);
            updateTaskAfterDispatch(task, result);
            announcementTaskRepository.save(task);
            results.add(result);
        }

        return results;
    }

    private AnnouncementTaskEntity findOrCreateTask(Event event, AnnouncementChannel channel) {
        return announcementTaskRepository.findByEventIdAndChannel(event.id(), channel)
                .orElseGet(
                        () -> new AnnouncementTaskEntity(
                                UUID.randomUUID(),
                                event.id(),
                                channel,
                                eventAnnouncementService.buildPayload(channel, event),
                                maxAttempts
                        )
                );
    }

    private void updateTaskAfterDispatch(AnnouncementTaskEntity task, AnnouncementDispatchResult result) {
        if (!result.attempted()) {
            task.markSkipped(result.detail());
            return;
        }

        if (result.success()) {
            task.markSent(result.statusCode(), result.detail());
            return;
        }

        task.markFailed(result.statusCode(), result.detail(), backoffSeconds);
    }
}
