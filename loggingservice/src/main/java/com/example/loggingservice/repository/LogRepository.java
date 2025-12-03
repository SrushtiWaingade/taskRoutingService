package com.example.loggingservice.repository;

import com.example.loggingservice.model.LogPayload;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends ElasticsearchRepository<LogPayload, String> {
}

