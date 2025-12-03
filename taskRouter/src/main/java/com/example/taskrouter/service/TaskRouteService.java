package com.example.taskrouter.service;

import com.example.taskrouter.dto.MessageRequestDTO;
import com.example.taskrouter.dto.TaskRouteResponse;
import com.example.taskrouter.model.Task;
import com.example.taskrouter.repository.TaskRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskRouteService {

    private static final Map<String, String> CHANNEL_TO_SERVICE = Map.of(
            "email", "Email Service",
            "sms", "SMS Service",
            "whatsapp", "WhatsApp Service"
    );

    private final TaskRouteRepository repo;
    private final RabbitTemplate rabbitTemplate;
    private final LoggingService loggingService;

    @Value("${rabbitmq.exchange.message:message.exchange}")
    private String messageExchange;

    @Value("${router.publish-retry.attempts:3}")
    private int publishRetryAttempts;

    @Value("${router.publish-retry.backoff-ms:100}")
    private int publishRetryBackoffMs;

    public TaskRouteResponse acceptAndRoute(MessageRequestDTO req) {
        String traceId = UUID.randomUUID().toString();

        String normalizedChannel = normalize(req.getChannel());
        String channelError = validateSupportedChannel(normalizedChannel);
        if (channelError != null) {
            loggingService.sendLog("task-router", traceId, "VALIDATION_FAILED",
                    Map.of("error", channelError), normalizedChannel != null ? normalizedChannel : "unknown", 
                    req.getTo() != null ? req.getTo() : "", null, "failure");
            return TaskRouteResponse.builder()
                    .status("VALIDATION_FAILED")
                    .traceId(traceId)
                    .details(channelError)
                    .duplicate(false)
                    .build();
        }

        String recipientError = validateRecipient(normalizedChannel, req.getTo());
        if (recipientError != null) {
            loggingService.sendLog("task-router", traceId, "VALIDATION_FAILED",
                    Map.of("error", recipientError), normalizedChannel, req.getTo(), null, "failure");
            return TaskRouteResponse.builder()
                    .status("VALIDATION_FAILED")
                    .traceId(traceId)
                    .details(recipientError)
                    .duplicate(false)
                    .build();
        }

        String uniqueMessageSignature = buildMessageSignature(normalizedChannel, req.getTo(), req.getBody());
        if (repo.existsByUniqueBody(uniqueMessageSignature)) {
            loggingService.sendLog("task-router", traceId, "DUPLICATE_SUPPRESSED",
                    Map.of(), normalizedChannel, req.getTo(), null, "failure");
            return TaskRouteResponse.builder()
                    .status("DUPLICATE_SUPPRESSED")
                    .duplicate(true)
                    .traceId(traceId)
                    .details("Message with identical channel, recipient, and body already processed.")
                    .build();
        }

        loggingService.sendLog("task-router", traceId, "RECEIVED",
                Map.of(), normalizedChannel, req.getTo(), null, "success");

        Task taskRecord = Task.builder()
                .traceId(traceId)
                .channel(normalizedChannel)
                .service(CHANNEL_TO_SERVICE.get(normalizedChannel))
                .to(req.getTo())
                .body(req.getBody())
                .timestamp(System.currentTimeMillis())
                .attempt(0)
                .uniqueBody(uniqueMessageSignature)
                .build();
        repo.save(taskRecord);

        boolean published = publishWithRetry(normalizedChannel, taskRecord, traceId);

        if (!published) {
            taskRecord.setAttempt(publishRetryAttempts);
            repo.save(taskRecord);
            loggingService.sendLog("task-router", traceId, "PUBLISH_FAILED_PERMANENT",
                    Map.of("exchange", messageExchange), normalizedChannel, req.getTo(), null, "failure");

            return TaskRouteResponse.builder()
                    .status("FAILED")
                    .traceId(traceId)
                    .details("Failed to route message after retry attempts.")
                    .duplicate(false)
                    .build();
        }

        loggingService.sendLog("task-router", traceId, "PUBLISHED_TO_EXCHANGE",
                Map.of("exchange", messageExchange), normalizedChannel, req.getTo(), null, "success");

        return TaskRouteResponse.builder()
                .status("ROUTED")
                .traceId(traceId)
                .details("Forwarded to " + CHANNEL_TO_SERVICE.get(normalizedChannel))
                .duplicate(false)
                .build();
    }

    private boolean publishWithRetry(String routingKey, Task task, String traceId) {
        int attempt = 0;
        String recipient = task.getTo();
        while (attempt < publishRetryAttempts) {
            try {
                attempt++;
                task.setAttempt(attempt);
                rabbitTemplate.convertAndSend(messageExchange, routingKey, task);
                return true;
            } catch (AmqpException ex) {
                loggingService.sendLog("task-router", traceId, "PUBLISH_FAILED_ATTEMPT",
                        Map.of("error", ex.getMessage()), routingKey, recipient, attempt, "retry");
                try {
                    Thread.sleep(publishRetryBackoffMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private String validateSupportedChannel(String channel) {
        if (!CHANNEL_TO_SERVICE.containsKey(channel)) {
            return "Unsupported channel: " + channel;
        }
        return null;
    }

    private String normalize(String channel) {
        return channel == null ? null : channel.trim().toLowerCase();
    }

    private String buildMessageSignature(String channel, String to, String body) {
        // Combine channel, to, and body to create a unique signature
        String combined = String.format("%s|%s|%s", 
            channel != null ? channel : "", 
            to != null ? to : "", 
            body != null ? body : "");
        
        if (!StringUtils.hasText(combined)) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash message for duplicate detection", e);
        }
    }

    private String validateRecipient(String channel, String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            return "Recipient 'to' field is required";
        }

        String trimmedRecipient = recipient.trim();

        if ("email".equals(channel)) {
            if (!isValidEmail(trimmedRecipient)) {
                return "For email channel, 'to' must be a valid email address";
            }
        } else if ("sms".equals(channel) || "whatsapp".equals(channel)) {
            if (!isValidPhoneNumber(trimmedRecipient)) {
                return "For " + channel + " channel, 'to' must be a 10-digit phone number";
            }
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Basic email regex pattern
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Must be exactly 10 digits
        String phonePattern = "^\\d{10}$";
        return phone.matches(phonePattern);
    }
}
