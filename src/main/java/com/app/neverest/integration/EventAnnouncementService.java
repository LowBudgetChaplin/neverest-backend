package com.app.neverest.integration;

import com.app.neverest.domain.Event;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventAnnouncementService {

    private final HttpClient httpClient;
    private final boolean whatsappEnabled;
    private final String whatsappWebhookUrl;
    private final boolean stravaEnabled;
    private final String stravaWebhookUrl;
    private final int timeoutSeconds;

    public EventAnnouncementService(
            @Value("${neverest.integrations.whatsapp.enabled:false}") boolean whatsappEnabled,
            @Value("${neverest.integrations.whatsapp.webhook-url:}") String whatsappWebhookUrl,
            @Value("${neverest.integrations.strava.enabled:false}") boolean stravaEnabled,
            @Value("${neverest.integrations.strava.webhook-url:}") String stravaWebhookUrl,
            @Value("${neverest.integrations.timeout-seconds:5}") int timeoutSeconds
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .build();
        this.whatsappEnabled = whatsappEnabled;
        this.whatsappWebhookUrl = whatsappWebhookUrl == null ? "" : whatsappWebhookUrl.trim();
        this.stravaEnabled = stravaEnabled;
        this.stravaWebhookUrl = stravaWebhookUrl == null ? "" : stravaWebhookUrl.trim();
        this.timeoutSeconds = Math.max(timeoutSeconds, 1);
    }

    public List<AnnouncementDispatchResult> publishEventCreated(Event event) {
        List<AnnouncementDispatchResult> results = new ArrayList<>();
        results.add(dispatchEventCreated(AnnouncementChannel.WHATSAPP, event));
        results.add(dispatchEventCreated(AnnouncementChannel.STRAVA, event));
        return results;
    }

    public AnnouncementDispatchResult dispatchEventCreated(AnnouncementChannel channel, Event event) {
        if (channel == AnnouncementChannel.WHATSAPP) {
            return dispatch(channel, whatsappEnabled, whatsappWebhookUrl, event);
        }
        if (channel == AnnouncementChannel.STRAVA) {
            return dispatch(channel, stravaEnabled, stravaWebhookUrl, event);
        }

        return new AnnouncementDispatchResult(channel, false, false, null, "Unsupported channel.");
    }

    private AnnouncementDispatchResult dispatch(
            AnnouncementChannel channel,
            boolean enabled,
            String webhookUrl,
            Event event
    ) {
        if (!enabled) {
            return new AnnouncementDispatchResult(channel, false, false, null, "Integration disabled.");
        }
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return new AnnouncementDispatchResult(channel, false, false, null, "Webhook URL not configured.");
        }

        String body = buildPayload(channel, event);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            String detail = success
                    ? "Delivered successfully."
                    : "Webhook responded with status " + response.statusCode() + ".";

            return new AnnouncementDispatchResult(channel, true, success, response.statusCode(), detail);
        } catch (Exception exception) {
            return new AnnouncementDispatchResult(channel, true, false, null, "Delivery error: " + exception.getMessage());
        }
    }

    public String buildPayload(AnnouncementChannel channel, Event event) {
        String message = "Nou eveniment: " + event.title()
                + " | " + event.activityType().name()
                + " | " + event.location()
                + " | startsAt=" + event.startsAt();

        return "{"
                + "\"type\":\"EVENT_CREATED\","
                + "\"channel\":\"" + escapeJson(channel.name()) + "\","
                + "\"eventId\":\"" + escapeJson(event.id().toString()) + "\","
                + "\"title\":\"" + escapeJson(event.title()) + "\","
                + "\"activityType\":\"" + escapeJson(event.activityType().name()) + "\","
                + "\"location\":\"" + escapeJson(event.location()) + "\","
                + "\"startsAt\":\"" + escapeJson(event.startsAt().toString()) + "\","
                + "\"pointsReward\":" + event.pointsReward() + ","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
