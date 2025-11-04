package com.NotificationService.NotificationService.repository;

import com.NotificationService.NotificationService.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserId(Integer userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<Notification> findByStatus(String status);

    List<Notification> findByNotificationType(String notificationType);
}

