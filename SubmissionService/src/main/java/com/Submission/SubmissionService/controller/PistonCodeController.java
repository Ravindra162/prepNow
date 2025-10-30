package com.Submission.SubmissionService.controller;

import com.Submission.SubmissionService.dto.PistonRuntimeResponse;
import com.Submission.SubmissionService.dto.RunCodeRequest;
import com.Submission.SubmissionService.dto.RunCodeResponse;
import com.Submission.SubmissionService.service.PistonApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PistonCodeController {

    private final PistonApiService pistonApiService;

    /**
     * Get list of available programming languages/runtimes
     * GET /api/code/runtimes
     */
    @GetMapping("/runtimes")
    public ResponseEntity<List<PistonRuntimeResponse>> getRuntimes() {
        log.info("GET /api/code/runtimes - Fetching available runtimes");
        List<PistonRuntimeResponse> runtimes = pistonApiService.getRuntimes();
        return ResponseEntity.ok(runtimes);
    }

    /**
     * Execute code directly using Piston API
     * POST /api/code/run
     */
    @PostMapping("/run")
    public ResponseEntity<RunCodeResponse> runCode(@Valid @RequestBody RunCodeRequest request) {
        log.info("POST /api/code/run - Executing {} code", request.getLanguage());
        RunCodeResponse response = pistonApiService.executeCode(request);
        return ResponseEntity.ok(response);
    }
}

