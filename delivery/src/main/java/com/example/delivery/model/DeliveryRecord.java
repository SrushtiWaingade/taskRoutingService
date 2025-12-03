package com.example.delivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("deliveries")
public class DeliveryRecord {
    @Id
    private String id;
    private String traceId;
    private String channel;
    private String to;
    private String body;
    private String status;  // SENT / FAILED
    private int attempts;
    private long timestamp;
}
