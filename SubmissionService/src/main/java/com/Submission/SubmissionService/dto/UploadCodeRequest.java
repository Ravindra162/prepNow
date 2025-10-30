package com.Submission.SubmissionService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadCodeRequest {
    @NotBlank(message = "Submission ID is required")
    private String submissionId;

    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Code content is required")
    private String content;

    private String filename;
}

