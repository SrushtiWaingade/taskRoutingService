package com.example.taskrouter.service;

import com.example.taskrouter.model.LogPayload;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LoggingService {
    private final RabbitTemplate rabbitTemplate;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Value("${rabbitmq.exchange.logs:logs.exchange}")
    private String logsExchange;

    public LoggingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendLog(String service, String traceId, String event,
                       Map<String, Object> meta, String channel, String recipient, 
                       Integer attempts, String status) {
        // Extract channel from meta if not provided explicitly, or use provided channel
        String extractedChannel = channel;
        if (extractedChannel == null && meta != null && meta.containsKey("channel")) {
            extractedChannel = (String) meta.get("channel");
        }
        if (extractedChannel != null) {
            // Convert to uppercase for readability (EMAIL, SMS, WHATSAPP)
            extractedChannel = extractedChannel.toUpperCase();
        }
        
        // Extract recipient from meta.to if not provided explicitly
        String extractedRecipient = recipient;
        if (extractedRecipient == null && meta != null && meta.containsKey("to")) {
            extractedRecipient = (String) meta.get("to");
        }
        
        // Build payload object from remaining meta fields (excluding channel and to)
        Map<String, Object> payload = new HashMap<>();
        if (meta != null) {
            for (Map.Entry<String, Object> entry : meta.entrySet()) {
                String key = entry.getKey();
                if (!key.equals("channel") && !key.equals("to")) {
                    payload.put(key, entry.getValue());
                }
            }
        }
        
        // Build LogPayload using builder pattern
        LogPayload.LogPayloadBuilder builder = LogPayload.builder()
                .traceId(traceId)
                .service(service)
                .event(event)
                .channel(extractedChannel)
                .recipient(extractedRecipient)
                .attempts(attempts)
                .timestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                .status(status);
        
        // Only include payload if it's not empty
//        if (!payload.isEmpty()) {
//            builder.payload(payload);
//        }
        
        LogPayload logPayload = builder.build();

        rabbitTemplate.convertAndSend(logsExchange, "", logPayload);
    }
}
