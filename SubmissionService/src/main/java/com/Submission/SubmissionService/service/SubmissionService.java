package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.domain.Submission;
import com.Submission.SubmissionService.domain.SubmissionStatus;
import com.Submission.SubmissionService.dto.CreateSubmissionRequest;
import com.Submission.SubmissionService.dto.SubmissionResponse;
import com.Submission.SubmissionService.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final NotificationPublisher notificationPublisher;
    private final EvaluationPublisher evaluationPublisher;

    public SubmissionResponse createSubmission(CreateSubmissionRequest request) {
        log.info("Creating submission for user {} and test {}", request.getUserId(), request.getTestId());

        Submission submission = Submission.builder()
                .userId(request.getUserId())
                .testId(request.getTestId())
                .createdAt(Instant.now())
                .status(SubmissionStatus.SUBMITTED)
                .metadata(request.getMetadata())
                .build();

        submission = submissionRepository.save(submission);
        log.info("Submission created with id: {}", submission.getId());

        // Send notification after successful submission
        try {
            Map<String, Object> metadata = request.getMetadata();
            if (metadata != null) {
                String assessmentName = (String) metadata.get("assessmentName");
                String companyName = (String) metadata.get("companyName");
                Integer timeTakenMinutes = (Integer) metadata.get("timeTakenMinutes");

                // Extract user info from metadata or use userId
                String userEmail = (String) metadata.get("userEmail");
                String userName = (String) metadata.get("userName");

                // If email not in metadata, try to parse from userId or use a placeholder
                if (userEmail == null) {
                    // We'll need to get this from AuthService in production
                    log.warn("User email not found in metadata for submission {}", submission.getId());
                } else {
                    Integer userId = Integer.parseInt(request.getUserId());

                    notificationPublisher.sendAssessmentSubmittedNotification(
                            userId,
                            userEmail,
                            userName != null ? userName : "User",
                            assessmentName != null ? assessmentName : "Assessment",
                            companyName != null ? companyName : "Company",
                            timeTakenMinutes != null ? timeTakenMinutes : 0
                    );
                    log.info("Assessment submitted notification sent for submission {}", submission.getId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send submission notification: {}", e.getMessage());
            // Don't fail the submission if notification fails
        }

        // Automatically publish evaluation request to queue
        try {
            evaluationPublisher.publishEvaluationRequest(
                    submission.getId(),
                    request.getUserId(),
                    request.getTestId()
            );
            log.info("Evaluation request published to queue for submission {}", submission.getId());
        } catch (Exception e) {
            log.error("Failed to publish evaluation request: {}", e.getMessage());
            // Don't fail the submission if queue publishing fails
        }

        return mapToResponse(submission);
    }

    public SubmissionResponse getSubmission(String id) {
        log.info("Fetching submission with id: {}", id);
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));
        return mapToResponse(submission);
    }

    public List<SubmissionResponse> getSubmissionsByUserId(String userId) {
        log.info("Fetching submissions for user: {}", userId);
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponse> getSubmissionsByTestId(String testId) {
        log.info("Fetching submissions for test: {}", testId);
        return submissionRepository.findByTestIdOrderByCreatedAtDesc(testId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponse> getSubmissionsByUserAndTest(String userId, String testId) {
        log.info("Fetching submissions for user {} and test {}", userId, testId);
        return submissionRepository.findByUserIdAndTestId(userId, testId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SubmissionResponse updateSubmissionStatus(String id, SubmissionStatus status) {
        log.info("Updating submission {} status to {}", id, status);
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        submission.setStatus(status);

        submission = submissionRepository.save(submission);
        return mapToResponse(submission);
    }

    public SubmissionResponse updateSubmissionScore(String id, Double totalScore, Double maxScore) {
        log.info("Updating submission {} score to {}/{}", id, totalScore, maxScore);
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        submission.setTotalScore(totalScore);
        submission.setMaxScore(maxScore);
        submission.setStatus(SubmissionStatus.COMPLETED);

        submission = submissionRepository.save(submission);
        return mapToResponse(submission);
    }

    public void deleteSubmission(String id) {
        log.info("Deleting submission with id: {}", id);
        submissionRepository.deleteById(id);
    }

    public List<SubmissionResponse> getAllSubmissions() {
        log.info("Fetching all submissions");
        return submissionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .userId(submission.getUserId())
                .testId(submission.getTestId())
                .createdAt(submission.getCreatedAt())
                .status(submission.getStatus())
                .totalScore(submission.getTotalScore())
                .maxScore(submission.getMaxScore())
                .metadata(submission.getMetadata())
                .evaluationId(submission.getEvaluationId())
                .build();
    }
}
