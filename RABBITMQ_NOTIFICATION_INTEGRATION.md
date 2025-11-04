# RabbitMQ Notification Integration Guide

## Overview

This document describes the RabbitMQ-based notification system integrated into the PrepNow platform. The system enables asynchronous notification delivery for assessment-related events.

## Architecture

### Message Flow
```
AssessmentService/SubmissionService → RabbitMQ → NotificationService → Email/SMTP
```

### Components

1. **Publishers** (AssessmentService, SubmissionService)
   - Publish notification messages to RabbitMQ when events occur
   - Continue operation even if RabbitMQ is unavailable

2. **RabbitMQ Message Broker**
   - Stores messages in durable queues
   - Routes messages using topic exchanges
   - Ensures reliable message delivery

3. **Consumer** (NotificationService)
   - Listens to notification queue
   - Processes messages and sends emails

## Setup Instructions

### 1. Start RabbitMQ

```bash
# Using Docker Compose
docker-compose -f docker-compose-rabbitmq.yml up -d

# Verify RabbitMQ is running
docker ps | grep rabbitmq
```

**Access RabbitMQ Management UI:**
- URL: http://localhost:15672
- Username: guest
- Password: guest

### 2. Dependencies Added

**AssessmentService & SubmissionService pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 3. Configuration

**application.properties (Both services):**
```properties
# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Notification Types

### 1. Assessment Started
**Trigger:** When a user starts an assessment  
**Publisher:** AssessmentService  
**Data Includes:**
- User details (name, email)
- Assessment name
- Company name
- Duration in minutes
- Start timestamp

### 2. Assessment Completed
**Trigger:** When a user submits an assessment  
**Publisher:** AssessmentService  
**Data Includes:**
- User details
- Assessment name
- Company name
- Completion timestamp
- Submission status message

### 3. Evaluation Completed
**Trigger:** When assessment evaluation is finished  
**Publisher:** SubmissionService  
**Data Includes:**
- User details
- Assessment name
- Total score and max score
- Percentage score
- Pass/fail status
- Evaluation timestamp

### 4. Submission Received
**Trigger:** When a submission is received for evaluation  
**Publisher:** SubmissionService  
**Data Includes:**
- User details
- Assessment name
- Submission timestamp
- Processing status message

## Implementation Details

### Message Structure

```java
public class NotificationMessage {
    private String notificationType;      // ASSESSMENT_STARTED, EVALUATION_COMPLETED, etc.
    private String recipientEmail;        // User's email address
    private String recipientName;         // User's name
    private Integer userId;               // User ID
    private String subject;               // Email subject
    private String templateName;          // Email template to use
    private Map<String, Object> templateData;  // Template variables
    private LocalDateTime createdAt;      // Message creation time
    private String priority;              // HIGH, MEDIUM, LOW
}
```

### Queue Configuration

- **Exchange:** `notification.exchange` (Topic Exchange)
- **Queue:** `notification.queue` (Durable)
- **Routing Key:** `notification.routing.key`
- **Message Format:** JSON

### NotificationPublisher (AssessmentService)

**Methods:**
1. `sendAssessmentStartedNotification()` - When assessment begins
2. `sendAssessmentCompletedNotification()` - When assessment is submitted
3. `sendAssessmentReminderNotification()` - For pending assessments

**Example Usage:**
```java
@Service
public class AssessmentAttemptService {
    private final NotificationPublisher notificationPublisher;
    
    public Map<String, Object> startAssessmentAttempt(Long assessmentId, Integer userRef) {
        // ... business logic ...
        
        // Send notification
        notificationPublisher.sendAssessmentStartedNotification(
            userRef,
            userEmail,
            userName,
            assessmentName,
            companyName,
            durationMinutes
        );
    }
}
```

### NotificationPublisher (SubmissionService)

**Methods:**
1. `sendEvaluationCompletedNotification()` - When evaluation is done
2. `sendSubmissionReceivedNotification()` - When submission is received

**Example Usage:**
```java
@Service
public class EvaluationService {
    private final NotificationPublisher notificationPublisher;
    
    public EvaluationResponse evaluateSubmission(String submissionId) {
        // ... evaluation logic ...
        
        // Send notification
        notificationPublisher.sendEvaluationCompletedNotification(
            userId,
            userEmail,
            userName,
            assessmentName,
            totalScore,
            maxScore,
            percentage
        );
    }
}
```

## Testing the Integration

### 1. Check RabbitMQ is Running
```bash
# Check container status
docker ps | grep rabbitmq

# Check RabbitMQ logs
docker logs rabbitmq
```

### 2. Monitor Messages in RabbitMQ UI
1. Open http://localhost:15672
2. Go to "Queues" tab
3. You should see `notification.queue`
4. Click on the queue to see messages

### 3. Test Notification Publishing

**Test Assessment Started Notification:**
```bash
# Start an assessment through the API
curl -X POST http://localhost:8081/api/assessments/{assessmentId}/start \
  -H "Authorization: Bearer {token}"
```

**Check RabbitMQ:**
- Messages should appear in the queue
- Check "Message rates" graph for activity

### 4. Service Logs

**AssessmentService:**
```
✓ Notification published to RabbitMQ: type=ASSESSMENT_STARTED, recipient=user@example.com
```

**SubmissionService:**
```
✓ Evaluation completion notification sent for user: 123
✓ Notification published to RabbitMQ: type=EVALUATION_COMPLETED, recipient=user@example.com
```

## Next Steps for NotificationService

The NotificationService needs to be implemented to:

1. **Consumer Implementation:**
   ```java
   @RabbitListener(queues = "notification.queue")
   public void handleNotification(NotificationMessage message) {
       // Process and send email
   }
   ```

2. **Email Integration:**
   - Add Spring Mail dependency
   - Configure SMTP settings
   - Create email templates

3. **Template Engine:**
   - Use Thymeleaf or Freemarker
   - Create HTML email templates
   - Support dynamic content

## Error Handling

### Publisher Side (Resilient)
- Notifications fail silently to prevent service disruption
- Errors are logged but don't affect main business logic
- Uses try-catch blocks around notification calls

### Consumer Side (To be implemented)
- Dead letter queue for failed messages
- Retry mechanism with exponential backoff
- Manual intervention for persistent failures

## Configuration for Production

### RabbitMQ Settings
```properties
# Connection pool
spring.rabbitmq.connection-timeout=10000
spring.rabbitmq.cache.connection.mode=CHANNEL
spring.rabbitmq.cache.connection.size=25

# Publisher confirmations
spring.rabbitmq.publisher-confirms=true
spring.rabbitmq.publisher-returns=true

# Consumer settings
spring.rabbitmq.listener.simple.concurrency=5
spring.rabbitmq.listener.simple.max-concurrency=10
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
```

### Security
```properties
# Use environment variables in production
spring.rabbitmq.username=${RABBITMQ_USERNAME}
spring.rabbitmq.password=${RABBITMQ_PASSWORD}
spring.rabbitmq.host=${RABBITMQ_HOST}
```

## Monitoring and Maintenance

### Metrics to Monitor
1. Message publish rate
2. Message consumption rate
3. Queue depth
4. Consumer lag
5. Failed message count

### Health Checks
```bash
# Check queue status
curl -u guest:guest http://localhost:15672/api/queues/%2F/notification.queue

# Check exchange
curl -u guest:guest http://localhost:15672/api/exchanges/%2F/notification.exchange
```

## Troubleshooting

### Issue: Messages not appearing in queue
**Solutions:**
- Check RabbitMQ is running: `docker ps`
- Verify connection settings in application.properties
- Check service logs for connection errors
- Ensure exchange and queue are created

### Issue: Services can't connect to RabbitMQ
**Solutions:**
- Check firewall settings (port 5672)
- Verify RabbitMQ is listening: `netstat -an | grep 5672`
- Check credentials in application.properties
- Review RabbitMQ logs: `docker logs rabbitmq`

### Issue: Messages stuck in queue
**Solutions:**
- Ensure NotificationService consumer is running
- Check consumer logs for processing errors
- Verify email/SMTP configuration
- Check dead letter queue for failed messages

## Benefits of This Architecture

1. **Loose Coupling:** Services don't need to know about each other
2. **Reliability:** Messages are persisted and guaranteed delivery
3. **Scalability:** Can scale notification processing independently
4. **Resilience:** Publishers continue working even if consumer is down
5. **Flexibility:** Easy to add new notification types or consumers

## Future Enhancements

1. **Multiple Notification Channels:**
   - SMS notifications
   - Push notifications
   - In-app notifications

2. **Advanced Features:**
   - Scheduled notifications
   - Batch processing
   - Priority queues
   - User notification preferences

3. **Analytics:**
   - Notification delivery tracking
   - Open/click rates
   - User engagement metrics

---

**Status:** ✅ RabbitMQ integration completed for AssessmentService and SubmissionService  
**Next:** Implement NotificationService consumer and email functionality

