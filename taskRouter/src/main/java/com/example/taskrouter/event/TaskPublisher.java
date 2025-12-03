package com.example.taskrouter.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskPublisher {
    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sendLog(String service, String traceId, String messageId, String event, Map<String, Object> meta) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("service", service);
        payload.put("traceId", traceId);
        payload.put("messageId", messageId);
        payload.put("event", event);
        payload.put("meta", meta);
        payload.put("timestamp", Instant.now().toEpochMilli());

        rabbitTemplate.convertAndSend("logs.exchange", "", payload);
    }
}
