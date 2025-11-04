# RabbitMQ Notification System Setup Guide

## Overview
This guide covers the complete setup of the RabbitMQ-based notification system for PrepNow platform. The system sends email notifications for:
- Assessment started
- Assessment submitted
- Assessment evaluated
- Assessment reminders

## Architecture

### Services Communication Flow
```
AssessmentService → RabbitMQ → NotificationService → Email (SMTP)
SubmissionService → RabbitMQ → NotificationService → Email (SMTP)
```

### Message Flow
1. **AssessmentService** publishes messages when:
   - User starts an assessment
   - User submits an assessment
   - Admin sends reminder

2. **SubmissionService** publishes messages when:
   - Evaluation is completed

3. **NotificationService** consumes messages and:
   - Saves notification to MongoDB
   - Sends email via SMTP
   - Updates notification status (SENT/FAILED)

## Prerequisites

### 1. RabbitMQ Container
```bash
# Start RabbitMQ
cd /home/ravindra162/Desktop/prepNow
docker-compose -f docker-compose-rabbitmq.yml up -d

# Verify RabbitMQ is running
docker ps | grep rabbitmq

# Access RabbitMQ Management UI
# URL: http://localhost:15672
# Username: guest
# Password: guest
```

### 2. MongoDB for NotificationService
```bash
# Verify MongoDB is running on port 27018
docker ps | grep notificationDB
```

### 3. Email Configuration
Update `/home/ravindra162/Desktop/prepNow/NotificationService/src/main/resources/application.properties`:

```properties
# For Gmail
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
app.email.from=noreply@prepnow.com
app.email.from-name=PrepNow Platform
```

**Important:** For Gmail, you need to:
1. Enable 2-factor authentication
2. Generate an App Password (not your regular password)
3. Use the App Password in the configuration

## RabbitMQ Configuration

### Exchange and Queue Setup
All services use the same exchange and queue configuration:

- **Exchange:** `notification.exchange` (Topic Exchange)
- **Queue:** `notification.queue` (Durable)
- **Routing Keys:**
  - `notification.assessment.started`
  - `notification.assessment.submitted`
  - `notification.assessment.evaluated`
  - `notification.assessment.reminder`

### Message Structure
All services use a consistent `NotificationMessage` DTO:

```java
{
  "notificationType": "ASSESSMENT_STARTED",
  "userId": 123,
  "userEmail": "user@example.com",
  "userName": "John Doe",
  "assessmentName": "Java Developer Assessment",
  "companyName": "TechCorp",
  "additionalData": {
    "durationMinutes": 60,
    "score": 85.5,
    "percentage": 85.5,
    "isPassed": true
  },
  "timestamp": "2025-11-03T20:30:00"
}
```

## Starting the Services

### 1. Start AssessmentService
```bash
cd /home/ravindra162/Desktop/prepNow/AssessmentService
mvn spring-boot:run
```
Port: 8081

### 2. Start SubmissionService
```bash
cd /home/ravindra162/Desktop/prepNow/SubmissionService
mvn spring-boot:run
```
Port: 8083

### 3. Start NotificationService
```bash
cd /home/ravindra162/Desktop/prepNow/NotificationService
mvn spring-boot:run
```
Port: 8086

## Integrating Notification Publishers

### In AssessmentService

The `NotificationPublisher` is already configured. You can use it in your services:

```java
@Service
@RequiredArgsConstructor
public class AssessmentAttemptService {
    
    private final NotificationPublisher notificationPublisher;
    
    public void startAssessment(Assessment assessment, User user) {
        // Your existing logic...
        
        // Send notification
        notificationPublisher.sendAssessmentStartedNotification(
            user.getId(),
            user.getEmail(),
            user.getName(),
            assessment.getName(),
            assessment.getCompany().getName(),
            assessment.getDuration()
        );
    }
    
    public void submitAssessment(Assessment assessment, User user, int timeTaken) {
        // Your existing logic...
        
        // Send notification
        notificationPublisher.sendAssessmentSubmittedNotification(
            user.getId(),
            user.getEmail(),
            user.getName(),
            assessment.getName(),
            assessment.getCompany().getName(),
            timeTaken
        );
    }
}
```

### In SubmissionService

The `NotificationPublisher` is already configured. Use it after evaluation:

```java
@Service
@RequiredArgsConstructor
public class EvaluationService {
    
    private final NotificationPublisher notificationPublisher;
    
    public void completeEvaluation(Evaluation evaluation) {
        // Your existing evaluation logic...
        
        // Calculate results
        double percentage = (evaluation.getTotalScore() / evaluation.getMaxScore()) * 100;
        boolean isPassed = percentage >= 70; // Your passing criteria
        
        // Send notification
        notificationPublisher.sendEvaluationCompletedNotification(
            evaluation.getUserId(),
            evaluation.getUserEmail(),
            evaluation.getUserName(),
            evaluation.getAssessmentName(),
            evaluation.getCompanyName(),
            evaluation.getTotalScore(),
            evaluation.getMaxScore(),
            percentage,
            isPassed
        );
    }
}
```

## Testing the Notification System

### 1. Test RabbitMQ Connection
Access RabbitMQ Management UI: http://localhost:15672
- Check if exchange `notification.exchange` exists
- Check if queue `notification.queue` exists
- Verify bindings are created

### 2. Test Notification Flow

#### Test Assessment Started Notification
```bash
# Start an assessment through your API
curl -X POST http://localhost:8081/api/assessments/{assessmentId}/start \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### Test Assessment Submitted Notification
```bash
# Submit an assessment
curl -X POST http://localhost:8081/api/assessments/{assessmentId}/submit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### Test Evaluation Completed Notification
```bash
# Complete evaluation (typically done by admin)
curl -X POST http://localhost:8083/api/evaluations/{submissionId}/evaluate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 3. Check Notification Status

#### Get User Notifications
```bash
curl http://localhost:8086/api/notifications/user/{userId}
```

#### Get Failed Notifications
```bash
curl http://localhost:8086/api/notifications/failed
```

## Monitoring

### RabbitMQ Management UI
- URL: http://localhost:15672
- Monitor message rates, queue depth, and consumer status

### Check Logs

#### NotificationService Logs
```bash
tail -f /home/ravindra162/Desktop/prepNow/logs/NotificationService.log
```

#### RabbitMQ Container Logs
```bash
docker logs -f rabbitmq
```

### MongoDB - Check Notifications
```bash
# Connect to MongoDB
docker exec -it notificationDB mongosh

# Use the database
use notificationDB

# View all notifications
db.notifications.find().pretty()

# View failed notifications
db.notifications.find({status: "FAILED"}).pretty()

# View notifications by user
db.notifications.find({userId: 123}).pretty()

# Count notifications by type
db.notifications.aggregate([
  { $group: { _id: "$notificationType", count: { $sum: 1 } } }
])
```

## Troubleshooting

### Issue: RabbitMQ Connection Failed
**Solution:**
```bash
# Check if RabbitMQ is running
docker ps | grep rabbitmq

# Restart RabbitMQ
docker-compose -f docker-compose-rabbitmq.yml restart

# Check RabbitMQ logs
docker logs rabbitmq
```

### Issue: Email Not Sending
**Solution:**
1. Check email configuration in `application.properties`
2. For Gmail, ensure you're using an App Password
3. Check if SMTP ports are open (587 for TLS)
4. View NotificationService logs for email errors

### Issue: Messages Not Being Consumed
**Solution:**
1. Check if NotificationService is running
2. Verify RabbitMQ bindings in Management UI
3. Check if queue has consumers
4. Review NotificationService logs

### Issue: Notifications Marked as FAILED
**Solution:**
1. Query failed notifications: `GET /api/notifications/failed`
2. Check `errorMessage` field in MongoDB
3. Common causes:
   - Invalid email configuration
   - SMTP server issues
   - Invalid user email addresses

## Email Configuration for Different Providers

### Gmail
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Outlook/Office365
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
```

### SendGrid
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
```

### Mailtrap (For Testing)
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your-mailtrap-username
spring.mail.password=your-mailtrap-password
```

## Next Steps

1. **Configure Email Settings**: Update email credentials in NotificationService
2. **Start All Services**: Ensure RabbitMQ, MongoDB, and all services are running
3. **Test End-to-End**: Start an assessment and verify email is received
4. **Monitor**: Use RabbitMQ Management UI and check logs
5. **Custom Templates**: Modify email templates in `EmailService.java` as needed

## API Endpoints

### NotificationService Endpoints
- `GET /api/notifications/user/{userId}` - Get user's notifications
- `GET /api/notifications/failed` - Get all failed notifications

## Additional Features to Implement

1. **Retry Mechanism**: Implement automatic retry for failed notifications
2. **Dead Letter Queue**: Handle permanently failed messages
3. **Notification Preferences**: Allow users to opt-in/out of notifications
4. **SMS Notifications**: Extend to support SMS via Twilio
5. **Push Notifications**: Add mobile push notifications
6. **Scheduled Reminders**: Implement cron jobs for periodic reminders

## Security Considerations

1. **Protect RabbitMQ**: Change default credentials in production
2. **Secure Email Credentials**: Use environment variables or secret management
3. **Rate Limiting**: Implement rate limiting to prevent email spam
4. **Email Validation**: Validate email addresses before sending
5. **Audit Trail**: Log all notification activities

## Performance Tuning

1. **Concurrent Consumers**: Increase consumer threads for high load
2. **Batch Processing**: Process multiple notifications together
3. **Connection Pooling**: Configure RabbitMQ connection pool
4. **Async Processing**: Ensure email sending doesn't block main thread

---

**Need Help?** Check the logs and RabbitMQ Management UI first. Most issues are related to configuration.

