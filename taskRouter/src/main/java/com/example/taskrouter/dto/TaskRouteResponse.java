package com.example.taskrouter.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskRouteResponse {
    String status;
    String traceId;
    boolean duplicate;
    String details;
}

