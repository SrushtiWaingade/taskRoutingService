package com.example.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String traceId;
    private String channel;
    private String to;
    private String body;
    private int attempt;
}
