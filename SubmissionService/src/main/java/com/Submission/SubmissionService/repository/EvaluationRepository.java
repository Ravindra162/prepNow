package com.Submission.SubmissionService.repository;

import com.Submission.SubmissionService.domain.Evaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationRepository extends MongoRepository<Evaluation, String> {
    Optional<Evaluation> findBySubmissionId(String submissionId);
}
