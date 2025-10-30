package com.Submission.SubmissionService.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "submission_files")
public class SubmissionFile {
    @Id
    private String id;

    @Indexed
    private String submissionId;

    @Indexed
    private String questionId;

    private String filename;
    private String language;
    private String storagePath;
    private String contentType;

    private Long sizeBytes;
    private Instant uploadedAt;

    private String content;
}

