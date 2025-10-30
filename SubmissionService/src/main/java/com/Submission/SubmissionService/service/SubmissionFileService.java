package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.domain.Submission;
import com.Submission.SubmissionService.domain.SubmissionFile;
import com.Submission.SubmissionService.dto.SubmissionFileResponse;
import com.Submission.SubmissionService.dto.UploadCodeRequest;
import com.Submission.SubmissionService.repository.SubmissionFileRepository;
import com.Submission.SubmissionService.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionFileService {

    private final SubmissionFileRepository fileRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionFileResponse uploadCode(UploadCodeRequest request) {
        log.info("Uploading code for submission {} and question {}", 
                request.getSubmissionId(), request.getQuestionId());

        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new RuntimeException("Submission not found: " + request.getSubmissionId()));

        String filename = request.getFilename();
        if (filename == null || filename.isEmpty()) {
            filename = "solution." + getFileExtension(request.getLanguage());
        }

        SubmissionFile file = SubmissionFile.builder()
                .submissionId(request.getSubmissionId())
                .questionId(request.getQuestionId())
                .filename(filename)
                .language(request.getLanguage())
                .content(request.getContent())
                .sizeBytes((long) request.getContent().length())
                .uploadedAt(Instant.now())
                .contentType("text/plain")
                .storagePath("local://" + request.getSubmissionId() + "/" + filename)
                .build();

        file = fileRepository.save(file);

        if (!submission.getFileIds().contains(file.getId())) {
            submission.getFileIds().add(file.getId());
            submissionRepository.save(submission);
        }

        log.info("Code uploaded successfully with id: {}", file.getId());
        return mapToResponse(file);
    }

    public SubmissionFileResponse getFile(String id) {
        log.info("Fetching file with id: {}", id);
        SubmissionFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found: " + id));
        return mapToResponse(file);
    }

    public List<SubmissionFileResponse> getFilesBySubmission(String submissionId) {
        log.info("Fetching files for submission: {}", submissionId);
        return fileRepository.findBySubmissionId(submissionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubmissionFileResponse> getFilesByQuestion(String questionId) {
        log.info("Fetching files for question: {}", questionId);
        return fileRepository.findByQuestionId(questionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteFile(String id) {
        log.info("Deleting file with id: {}", id);
        fileRepository.deleteById(id);
    }

    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "python" -> "py";
            case "javascript" -> "js";
            case "cpp", "c++" -> "cpp";
            case "c" -> "c";
            case "go" -> "go";
            case "rust" -> "rs";
            default -> "txt";
        };
    }

    private SubmissionFileResponse mapToResponse(SubmissionFile file) {
        return SubmissionFileResponse.builder()
                .id(file.getId())
                .submissionId(file.getSubmissionId())
                .questionId(file.getQuestionId())
                .filename(file.getFilename())
                .language(file.getLanguage())
                .storagePath(file.getStoragePath())
                .sizeBytes(file.getSizeBytes())
                .uploadedAt(file.getUploadedAt())
                .content(file.getContent())
                .build();
    }
}

