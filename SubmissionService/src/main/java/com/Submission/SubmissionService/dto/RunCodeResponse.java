package com.Submission.SubmissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeResponse {
    private String language;
    private String version;
    private RunResult run;
    private CompileResult compile;
    private String message; // Error message if any

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunResult {
        private String stdout;
        private String stderr;
        private String output;
        private Integer code;
        private String signal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompileResult {
        private String stdout;
        private String stderr;
        private String output;
        private Integer code;
        private String signal;
    }
}

