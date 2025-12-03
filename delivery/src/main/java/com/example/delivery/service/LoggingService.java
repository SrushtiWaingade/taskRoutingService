package com.example.delivery.service;

import com.example.delivery.dto.LogPayload;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LoggingService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public void sendLog(String traceId, String event, String channel, String to, String status, int attempts) {

//        Map<String, Object> log = new HashMap<>();
//        log.put("service", "delivery-service");
//        log.put("traceId", traceId);
//        log.put("event", event);
//        log.put("meta", meta);
//        log.put("timestamp", new Date());

        LogPayload.LogPayloadBuilder builder = LogPayload.builder()
                .traceId(traceId)
                .service("delivery-service")
                .event(event)
                .channel(channel)
                .recipient(to)
                .timestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                .attempts(attempts)
                .status(status);

        LogPayload logPayload = builder.build();

        rabbitTemplate.convertAndSend("logs.exchange", "", logPayload);
    }
}
