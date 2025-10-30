package com.Submission.SubmissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PistonRuntimeResponse {
    private String language;
    private String version;
    private List<String> aliases;
    private String runtime;
}

