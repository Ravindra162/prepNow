package com.Submission.SubmissionService.config;

import com.Submission.SubmissionService.domain.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        try {
            log.info("Creating MongoDB indexes...");

            IndexOperations submissionOps = mongoTemplate.indexOps(Submission.class);
            submissionOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC));
            submissionOps.ensureIndex(new Index().on("testId", Sort.Direction.ASC));
            submissionOps.ensureIndex(new Index().on("status", Sort.Direction.ASC));
            submissionOps.ensureIndex(new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC));
            submissionOps.ensureIndex(new Index()
                    .on("testId", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC));

            IndexOperations fileOps = mongoTemplate.indexOps(SubmissionFile.class);
            fileOps.ensureIndex(new Index().on("submissionId", Sort.Direction.ASC));
            fileOps.ensureIndex(new Index().on("questionId", Sort.Direction.ASC));

            IndexOperations executionOps = mongoTemplate.indexOps(CodeExecution.class);
            executionOps.ensureIndex(new Index().on("submissionId", Sort.Direction.ASC));
            executionOps.ensureIndex(new Index().on("questionId", Sort.Direction.ASC));
            executionOps.ensureIndex(new Index().on("status", Sort.Direction.ASC));
            executionOps.ensureIndex(new Index()
                    .on("submissionId", Sort.Direction.ASC)
                    .on("executedAt", Sort.Direction.DESC));

            IndexOperations evaluationOps = mongoTemplate.indexOps(Evaluation.class);
            evaluationOps.ensureIndex(new Index().on("submissionId", Sort.Direction.ASC).unique());

            log.info("MongoDB indexes created successfully");
        } catch (Exception e) {
            log.warn("Failed to create MongoDB indexes. This may be due to authentication or permissions. The application will continue without indexes: {}", e.getMessage());
        }
    }
}
