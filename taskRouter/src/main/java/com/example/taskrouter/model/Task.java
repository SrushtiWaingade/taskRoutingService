package com.example.taskrouter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="tasks")
public class Task {
    @Id
    private String id;

    private String traceId;
    private String channel;
    private String service;
    private String to;
    private String body;
    @JsonIgnore
    private long timestamp;
    private int attempt;
    @Indexed(unique = true)
    @JsonIgnore
    private String uniqueBody;
}
