package com.example.loggingservice.service;

import com.example.loggingservice.model.LogPayload;
import com.example.loggingservice.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final LogRepository logRepository;

    @Value("${elasticsearch.index.name}")
    private String indexName;

    @RabbitListener(queues = "logs.queue")
    public void saveLog(LogPayload logPayload) {
        System.out.println("Received Log: " + logPayload.toString());
        try {
            logRepository.save(logPayload);
            System.out.println("Successfully saved log to Elasticsearch index: "+ indexName);
        } catch (Exception e) {
            System.out.println("Error saving log to Elasticsearch: "+ e.getMessage() + e);
        }
    }
}

