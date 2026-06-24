package com.app.neverest.integration;

import com.app.neverest.domain.Event;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventAnnouncementService {

    private final HttpClient httpClient;
    private final boolean whatsappEnabled;
    private final String whatsappWebhookUrl;
    private final String whatsappToken;
    private final String whatsappGroupId;
    private final boolean stravaEnabled;
    private final String stravaWebhookUrl;
    private final int timeoutSeconds;

    public EventAnnouncementService(
            @Value("${neverest.integrations.whatsapp.enabled:false}") boolean whatsappEnabled,
            @Value("${neverest.integrations.whatsapp.webhook-url:}") String whatsappWebhookUrl,
            @Value("${neverest.integrations.whatsapp.token:}") String whatsappToken,
            @Value("${neverest.integrations.whatsapp.group-id:}") String whatsappGroupId,
            @Value("${neverest.integrations.strava.enabled:false}") boolean stravaEnabled,
            @Value("${neverest.integrations.strava.webhook-url:}") String stravaWebhookUrl,
            @Value("${neverest.integrations.timeout-seconds:5}") int timeoutSeconds
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .build();
        this.whatsappEnabled = whatsappEnabled;
        this.whatsappWebhookUrl = whatsappWebhookUrl == null ? "" : whatsappWebhookUrl.trim();
        this.whatsappToken = whatsappToken == null ? "" : whatsappToken.trim();
        this.whatsappGroupId = whatsappGroupId == null ? "" : whatsappGroupId.trim();
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
            if (whatsappEnabled && !whatsappToken.isBlank() && !whatsappGroupId.isBlank()) {
                return dispatchWhapi(event);
            }
            return dispatch(channel, whatsappEnabled, whatsappWebhookUrl, event);
        }
        if (channel == AnnouncementChannel.STRAVA) {
            return dispatch(channel, stravaEnabled, stravaWebhookUrl, event);
        }

        return new AnnouncementDispatchResult(channel, false, false, null, "Unsupported channel.");
    }

    /**
     * Posts the announcement straight to the Whapi.Cloud send-text endpoint:
     *   POST {webhook-url}  Authorization: Bearer {token}
     *   { "to": "{group-id}", "body": "{message}" }
     */
    private AnnouncementDispatchResult dispatchWhapi(Event event) {
        if (whatsappWebhookUrl.isBlank()) {
            return new AnnouncementDispatchResult(
                    AnnouncementChannel.WHATSAPP, false, false, null, "WhatsApp gateway URL not configured.");
        }

        String body = "{"
                + "\"to\":\"" + escapeJson(whatsappGroupId) + "\","
                + "\"body\":\"" + escapeJson(buildMessage(event)) + "\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(whatsappWebhookUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + whatsappToken)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            String detail = success
                    ? "Delivered to WhatsApp group."
                    : "Whapi responded with status " + response.statusCode() + ": " + response.body();

            return new AnnouncementDispatchResult(
                    AnnouncementChannel.WHATSAPP, true, success, response.statusCode(), detail);
        } catch (Exception exception) {
            return new AnnouncementDispatchResult(
                    AnnouncementChannel.WHATSAPP, true, false, null, "Delivery error: " + exception.getMessage());
        }
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

    private static final DateTimeFormatter MESSAGE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String buildMessage(Event event) {
        StringBuilder msg = new StringBuilder();
        msg.append("🏃 Eveniment nou: ").append(event.title())
                .append("\n📍 ").append(event.location())
                .append("\n🗓️ ").append(event.startsAt().format(MESSAGE_DATE_FORMAT))
                .append("\n⚡ +").append(event.pointsReward()).append(" puncte");
        if (event.description() != null && !event.description().isBlank()) {
            msg.append("\n\n").append(event.description());
        }
        String mapUrl = cleanMapUrl(event.routeMapUrl());
        if (mapUrl != null) {
            msg.append("\n🗺️ Traseu: ").append(mapUrl);
        }
        return msg.toString();
    }

    static String cleanMapUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String url = raw.trim();

        Matcher src = Pattern.compile("src\\s*=\\s*\"([^\"]+)\"").matcher(url);
        if (src.find()) {
            url = src.group(1);
        } else {
            Matcher src2 = Pattern.compile("src\\s*=\\s*'([^']+)'").matcher(url);
            if (src2.find()) {
                url = src2.group(1);
            }
        }

        if (!url.startsWith("http")) {
            return null;
        }

        if (url.contains("/maps/embed")) {
            Matcher lng = Pattern.compile("!2d(-?\\d+(?:\\.\\d+)?)").matcher(url);
            Matcher lat = Pattern.compile("!3d(-?\\d+(?:\\.\\d+)?)").matcher(url);
            if (lng.find() && lat.find()) {
                return "https://www.google.com/maps/search/?api=1&query="
                        + lat.group(1) + "," + lng.group(1);
            }
        }
        return url;
    }

    public String buildPayload(AnnouncementChannel channel, Event event) {
        return "{"
                + "\"type\":\"EVENT_CREATED\","
                + "\"channel\":\"" + escapeJson(channel.name()) + "\","
                + "\"eventId\":\"" + escapeJson(event.id().toString()) + "\","
                + "\"title\":\"" + escapeJson(event.title()) + "\","
                + "\"activityType\":\"" + escapeJson(event.activityType().name()) + "\","
                + "\"location\":\"" + escapeJson(event.location()) + "\","
                + "\"startsAt\":\"" + escapeJson(event.startsAt().toString()) + "\","
                + "\"pointsReward\":" + event.pointsReward() + ","
                + "\"recurrence\":\"" + escapeJson(event.recurrence() == null ? "NONE" : event.recurrence().name()) + "\","
                + "\"description\":\"" + escapeJson(event.description()) + "\","
                + "\"routeMapUrl\":\"" + escapeJson(event.routeMapUrl()) + "\","
                + "\"stravaClubUrl\":\"" + escapeJson(event.stravaClubUrl()) + "\","
                + "\"whatsappGroupUrl\":\"" + escapeJson(event.whatsappGroupUrl()) + "\","
                + "\"message\":\"" + escapeJson(buildMessage(event)) + "\""
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
