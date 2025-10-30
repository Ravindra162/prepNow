package com.Submission.SubmissionService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeRequest {
    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Code is required")
    private String code;

    private String version; // Optional: SemVer version selector
    private String stdin; // Input to pass to the program
    private List<String> args; // Arguments to pass to the program
    private Integer runTimeout; // Max time in ms for run stage (default: 3000)
    private Integer compileTimeout; // Max time in ms for compile stage (default: 10000)
    private Long compileMemoryLimit; // Max memory for compile in bytes (default: -1)
    private Long runMemoryLimit; // Max memory for run in bytes (default: -1)
}

