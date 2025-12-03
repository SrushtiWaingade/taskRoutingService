package com.example.taskrouter.repository;

import com.example.taskrouter.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRouteRepository extends MongoRepository<Task, String> {
    boolean existsByUniqueBody(String uniqueBody);
}
