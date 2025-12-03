package com.example.delivery.repository;

import com.example.delivery.model.DeliveryRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeliveryRepository extends MongoRepository<DeliveryRecord, String> {
}
