package com.NotificationService.NotificationService.consumer;

import com.NotificationService.NotificationService.config.RabbitMQConfig;
import com.NotificationService.NotificationService.dto.NotificationMessage;
import com.NotificationService.NotificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(NotificationMessage message) {
        log.info("Received notification message: {} for user: {}",
            message.getNotificationType(), message.getUserId());

        try {
            notificationService.processNotification(message);
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
            // The message will be requeued or sent to DLQ based on configuration
        }
    }
}

