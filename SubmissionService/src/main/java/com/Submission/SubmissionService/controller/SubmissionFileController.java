package com.Submission.SubmissionService.controller;

import com.Submission.SubmissionService.dto.SubmissionFileResponse;
import com.Submission.SubmissionService.dto.UploadCodeRequest;
import com.Submission.SubmissionService.service.SubmissionFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SubmissionFileController {

    private final SubmissionFileService fileService;

    @PostMapping
    public ResponseEntity<SubmissionFileResponse> uploadCode(
            @Valid @RequestBody UploadCodeRequest request) {
        log.info("POST /api/files - Uploading code");
        SubmissionFileResponse response = fileService.uploadCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionFileResponse> getFile(@PathVariable String id) {
        log.info("GET /api/files/{} - Fetching file", id);
        SubmissionFileResponse response = fileService.getFile(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<SubmissionFileResponse>> getFilesBySubmission(
            @PathVariable String submissionId) {
        log.info("GET /api/files/submission/{} - Fetching files by submission", submissionId);
        List<SubmissionFileResponse> responses = fileService.getFilesBySubmission(submissionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<SubmissionFileResponse>> getFilesByQuestion(
            @PathVariable String questionId) {
        log.info("GET /api/files/question/{} - Fetching files by question", questionId);
        List<SubmissionFileResponse> responses = fileService.getFilesByQuestion(questionId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        log.info("DELETE /api/files/{} - Deleting file", id);
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}

