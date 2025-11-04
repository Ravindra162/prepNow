package com.NotificationService.NotificationService.controller;

import com.NotificationService.NotificationService.entity.Notification;
import com.NotificationService.NotificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Integer userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<Notification>> getFailedNotifications() {
        List<Notification> notifications = notificationService.getFailedNotifications();
        return ResponseEntity.ok(notifications);
    }
}
