package com.example.loggingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "logs")
public class LogPayload {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String traceId;

    @Field(type = FieldType.Text)
    private String service;

    @Field(type = FieldType.Text)
    private String event;

    @Field(type = FieldType.Text)
    private String channel;

    @Field(type = FieldType.Text)
    private String recipient;

    @Field(type = FieldType.Text)
    private Integer attempts;

    @Field(type = FieldType.Object)
    private Map<String, Object> payload;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private String timestamp;

    @Field(type = FieldType.Text)
    private String status;
}

