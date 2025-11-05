# Automated Evaluation Queue System - Implementation Guide

## Overview
Implemented an automated evaluation system using RabbitMQ queues. When an assessment is submitted, it's automatically added to an evaluation queue and processed asynchronously without requiring manual API calls.

## Architecture

### Before (Manual)
```
User submits → Submission created → Wait for manual API call → Evaluate
```

### After (Automated)
```
User submits → Submission created → Published to Queue → Auto-Evaluated → Results stored → Email sent
```

## Components Implemented

### 1. RabbitMQ Configuration (RabbitMQConfig.java)
Added new evaluation exchange and queue:
- **Exchange**: `evaluation.exchange`
- **Queue**: `evaluation.queue` (durable)
- **Routing Key**: `evaluation.request`

### 2. Evaluation Request Message (EvaluationRequestMessage.java)
DTO for queue messages containing:
- `submissionId` - ID of the submission to evaluate
- `userId` - User who submitted
- `testId` - Assessment/test ID
- `requestedAt` - Timestamp
- `retryCount` - Number of retry attempts

### 3. Evaluation Publisher (EvaluationPublisher.java)
Service that publishes evaluation requests to the queue:
```java
publishEvaluationRequest(submissionId, userId, testId)
```
- Automatically called when submission is created
- Non-blocking - doesn't fail submission if queue is down

### 4. Evaluation Consumer (EvaluationConsumer.java)
RabbitMQ listener that automatically processes evaluations:
- Listens to `evaluation.queue`
- Calls `EvaluationService.evaluateSubmission()`
- Handles retries (up to 3 attempts)
- Logs errors for failed evaluations

### 5. Updated SubmissionService
Modified to automatically publish evaluation requests:
```java
// After creating submission
evaluationPublisher.publishEvaluationRequest(
    submission.getId(),
    request.getUserId(),
    request.getTestId()
);
```

## Flow Diagram

```
┌─────────────────┐
│ User Submits    │
│ Assessment      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ SubmissionService│
│ creates          │
│ submission       │
└────────┬────────┘
         │
         ├─────────────────┐
         │                 │
         ▼                 ▼
┌─────────────────┐  ┌─────────────────┐
│ Notification    │  │ Evaluation      │
│ Queue           │  │ Queue           │
│ (Email)         │  │ (Auto-Eval)     │
└────────┬────────┘  └────────┬────────┘
         │                    │
         ▼                    ▼
┌─────────────────┐  ┌─────────────────┐
│ NotificationSvc │  │ EvaluationSvc   │
│ sends email     │  │ evaluates       │
└─────────────────┘  └────────┬────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │ Results stored  │
                     │ Status updated  │
                     │ Email sent      │
                     └─────────────────┘
```

## Benefits

### 1. **Automatic Processing**
- No need to manually hit evaluation endpoint
- Submissions are evaluated immediately after creation
- Asynchronous processing doesn't block user response

### 2. **Scalability**
- Multiple consumers can process queue in parallel
- Queue handles backlog if evaluation service is busy
- Retries failed evaluations automatically

### 3. **Reliability**
- Messages persist in queue (durable)
- Failed evaluations are retried (up to 3 times)
- System continues working even if evaluation temporarily fails

### 4. **Decoupling**
- Submission service doesn't depend on evaluation service
- Queue acts as buffer between services
- Services can be scaled independently

## Configuration

### RabbitMQ Queues Created
1. **evaluation.queue**
   - Purpose: Holds evaluation requests
   - Durable: Yes
   - Consumers: EvaluationConsumer
   
2. **notification.queue** (existing)
   - Purpose: Holds notification requests
   - Consumers: NotificationConsumer

### Queue Behavior
- **Retry Policy**: 3 attempts before giving up
- **Persistence**: Messages are durable (survive restart)
- **Acknowledgment**: Manual (consumer confirms processing)

## Testing

### 1. Submit an Assessment
```bash
# User submits assessment via frontend
# Submission is created automatically
```

### 2. Check Logs
```bash
# SubmissionService logs:
"Submission created with id: abc123"
"Evaluation request published to queue for submission abc123"

# EvaluationConsumer logs:
"Received evaluation request for submission: abc123"
"Successfully evaluated submission: abc123"
```

### 3. Check RabbitMQ Management UI
- Go to: http://localhost:15672 (guest/guest)
- Navigate to "Queues" tab
- See `evaluation.queue` with message count

### 4. Verify Results
- Check MongoDB/Database for evaluation results
- Check user's "My Tests" page for scores
- Check email for "Assessment Evaluated" notification

## Monitoring

### Key Metrics to Monitor
1. **Queue Size**: `evaluation.queue` message count
2. **Consumer Rate**: Messages processed per second
3. **Failed Evaluations**: Check logs for errors
4. **Retry Count**: Messages being retried

### RabbitMQ Dashboard
- URL: http://localhost:15672
- Username: guest
- Password: guest
- Check "Queues" → "evaluation.queue"

## Troubleshooting

### Queue Not Processing
**Symptom**: Messages stay in queue
**Solution**: 
- Check if SubmissionService is running
- Check logs for consumer errors
- Verify RabbitMQ connection

### Evaluation Fails
**Symptom**: Error in logs, no scores
**Solution**:
- Check if questions have correct answers defined
- Check if test cases are configured
- Check external API (Piston) is accessible

### Too Many Retries
**Symptom**: Same submission retried 3+ times
**Solution**:
- Check evaluation service logs for error details
- Fix underlying issue (missing data, API down)
- Message will go to DLQ after max retries

## API Endpoints

### Manual Evaluation (Still Available)
```http
POST /api/evaluations/submission/{submissionId}
```
You can still manually trigger evaluation if needed.

### Get Evaluation Status
```http
GET /api/evaluations/submission/{submissionId}
```
Check if submission has been evaluated.

## Future Enhancements

### 1. Dead Letter Queue (DLQ)
Set up DLQ for permanently failed evaluations:
```java
@Bean
public Queue evaluationDLQ() {
    return new Queue("evaluation.dlq", true);
}
```

### 2. Priority Queue
Prioritize certain submissions:
```java
// High priority for paid users
message.setPriority(10);
```

### 3. Batch Processing
Process multiple evaluations together:
```java
@RabbitListener(queues = EVALUATION_QUEUE, containerFactory = "batchContainerFactory")
public void consumeBatch(List<EvaluationRequestMessage> messages) {
    // Process batch
}
```

### 4. Scheduled Retry
Retry failed evaluations at specific intervals:
```java
@Scheduled(fixedDelay = 3600000) // Every hour
public void retryFailedEvaluations() {
    // Find FAILED evaluations and requeue
}
```

## Files Modified/Created

### Created:
1. `EvaluationRequestMessage.java` - DTO for queue messages
2. `EvaluationPublisher.java` - Publishes to evaluation queue
3. `EvaluationConsumer.java` - Consumes and processes evaluations

### Modified:
1. `RabbitMQConfig.java` - Added evaluation queue configuration
2. `SubmissionService.java` - Auto-publish evaluation requests

### Existing (No changes):
1. `EvaluationService.java` - Evaluation logic unchanged
2. `EvaluationController.java` - Manual endpoint still available

## Deployment Steps

1. **Rebuild SubmissionService**
   ```bash
   cd SubmissionService
   mvn clean package -DskipTests
   ```

2. **Restart SubmissionService**
   ```bash
   mvn spring-boot:run
   ```

3. **Verify Queues Created**
   - Check RabbitMQ dashboard
   - Look for `evaluation.queue`

4. **Test Submission**
   - Submit an assessment
   - Watch logs for automatic evaluation

## Success Indicators

✅ Submission created successfully
✅ "Evaluation request published" in logs
✅ "Received evaluation request" in consumer logs
✅ "Successfully evaluated" in logs
✅ Scores appear in database
✅ User sees results in "My Tests"
✅ Evaluation email sent

The system is now **fully automated**! 🎉

