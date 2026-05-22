package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.portfolio.port_view.config.SlackProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final SlackProperties slackProperties;

    public void sendText(String text) {
        if (!slackProperties.isEnabled()) {
            log.info("Slack notification skipped. slack.enabled=false");
            return;
        }

        String webhookUrl = normalizeWebhookUrl(slackProperties.getWebhookUrl());

        if (webhookUrl.isBlank()) {
            log.warn("Slack notification skipped. slack.webhook-url is blank");
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(buildPayload(text)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "Slack notification failed. statusCode={}, body={}",
                        response.statusCode(),
                        response.body()
                );
            } else {
                log.info("Slack notification sent. statusCode={}", response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Slack notification failed. errorType={}", e.getClass().getSimpleName());
        } catch (IllegalArgumentException | IOException e) {
            log.warn("Slack notification failed. errorType={}", e.getClass().getSimpleName());
        }
    }

    private String buildPayload(String text) {
        return "{\"text\":\"" + jsonEscape(text) + "\"}";
    }

    private String normalizeWebhookUrl(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();

        if (normalized.length() >= 2
                && normalized.startsWith("\"")
                && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        return normalized;
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }

        return sb.toString();
    }
}
