package com.example.taskrouter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogPayload {
    private String id;
    private String traceId;
    private String service;
    private String event;
    private String channel;
    private String recipient;
    private Integer attempts;
    private Map<String, Object> payload;
    private String timestamp;
    private String status;
}

