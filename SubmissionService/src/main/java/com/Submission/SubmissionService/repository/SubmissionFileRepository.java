package com.Submission.SubmissionService.repository;

import com.Submission.SubmissionService.domain.SubmissionFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionFileRepository extends MongoRepository<SubmissionFile, String> {
    List<SubmissionFile> findBySubmissionId(String submissionId);
    List<SubmissionFile> findByQuestionId(String questionId);
    Optional<SubmissionFile> findBySubmissionIdAndQuestionId(String submissionId, String questionId);
}

