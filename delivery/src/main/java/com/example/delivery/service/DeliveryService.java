package com.example.delivery.service;

import com.example.delivery.dto.MessageDTO;
import com.example.delivery.model.DeliveryRecord;
import com.example.delivery.repository.DeliveryRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class DeliveryService {
    @Autowired
    DeliveryRepository repo;

    @Autowired
    LoggingService loggingService;

    private final Random random = new Random();

    @RabbitListener(queues = "email")
    public void processEmail(MessageDTO msg) {
        process(msg, "EMAIL");
    }

    @RabbitListener(queues = "sms")
    public void processSms(MessageDTO msg) {
        process(msg, "SMS");
    }

    @RabbitListener(queues = "whatsapp")
    public void processWhatsapp(MessageDTO msg) {
        process(msg, "WHATSAPP");
    }

    private void process(MessageDTO msg, String channel) {
        System.out.println("processing :" + msg.toString() +" for channel :" + channel);
        int attempts = 0;
        boolean delivered = false;

        while (attempts < 3 && !delivered) {
            attempts++;

            try {
                // simulate 20% random failure
                if (random.nextInt(100) < 20) {
                    throw new RuntimeException("Simulated send failure");
                }

                // Success → save in DB
                DeliveryRecord record = new DeliveryRecord(
                        null,
                        msg.getTraceId(),
                        channel,
                        msg.getTo(),
                        msg.getBody(),
                        "SENT",
                        attempts,
                        System.currentTimeMillis()
                );

                repo.save(record);

                delivered = true;

                loggingService.sendLog(msg.getTraceId(), "DELIVERED", channel, msg.getTo(), record.getStatus(), attempts);

            } catch (Exception e) {
                loggingService.sendLog(msg.getTraceId(), "DELIVERY_FAILED_ATTEMPT", channel, msg.getTo(), "FAILED",attempts);

                // wait before retry
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        // permanent failure
        if (!delivered) {
            DeliveryRecord record = new DeliveryRecord(
                    null,
                    msg.getTraceId(),
                    channel,
                    msg.getTo(),
                    msg.getBody(),
                    "FAILED",
                    attempts,
                    System.currentTimeMillis()
            );
            repo.save(record);

            loggingService.sendLog(msg.getTraceId(), "DELIVERY_PERMANENT_FAILURE",channel, msg.getTo(), record.getStatus(),attempts);
        }
    }
}
