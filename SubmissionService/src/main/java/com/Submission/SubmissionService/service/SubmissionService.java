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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

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
        if (status == SubmissionStatus.COMPLETED) {
            submission.setSubmittedAt(Instant.now());
        }

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
        submission.setSubmittedAt(Instant.now());

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
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .totalScore(submission.getTotalScore())
                .maxScore(submission.getMaxScore())
                .metadata(submission.getMetadata())
                .fileIds(submission.getFileIds())
                .evaluationId(submission.getEvaluationId())
                .build();
    }
}

