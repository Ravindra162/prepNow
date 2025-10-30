package com.Submission.SubmissionService.repository;

import com.Submission.SubmissionService.domain.Submission;
import com.Submission.SubmissionService.domain.SubmissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findByUserId(String userId);
    List<Submission> findByTestId(String testId);
    List<Submission> findByUserIdAndTestId(String userId, String testId);
    List<Submission> findByStatus(SubmissionStatus status);
    List<Submission> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Submission> findByTestIdOrderByCreatedAtDesc(String testId);
    List<Submission> findByCreatedAtBetween(Instant start, Instant end);
}

