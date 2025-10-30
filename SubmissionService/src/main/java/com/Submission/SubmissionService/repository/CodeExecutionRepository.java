package com.Submission.SubmissionService.repository;

import com.Submission.SubmissionService.domain.CodeExecution;
import com.Submission.SubmissionService.domain.ExecutionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeExecutionRepository extends MongoRepository<CodeExecution, String> {
    List<CodeExecution> findBySubmissionId(String submissionId);
    List<CodeExecution> findByQuestionId(String questionId);
    List<CodeExecution> findBySubmissionIdOrderByExecutedAtDesc(String submissionId);
    List<CodeExecution> findByStatus(ExecutionStatus status);
    List<CodeExecution> findByFileId(String fileId);
}

