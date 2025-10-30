package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.dto.PistonRuntimeResponse;
import com.Submission.SubmissionService.dto.RunCodeRequest;
import com.Submission.SubmissionService.dto.RunCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class PistonApiService {

    @Value("${piston.api.url:https://emkc.org/api/v2/piston}")
    private String pistonApiUrl;

    private final RestTemplate restTemplate;
    private final Lock rateLimitLock = new ReentrantLock();
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 250; // 250ms to be safe (API limit is 200ms)

    public PistonApiService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Enforce rate limiting for Piston API (1 request per 200ms)
     */
    private void enforceRateLimit() {
        rateLimitLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;

            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
                long waitTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
                log.debug("Rate limiting: waiting {}ms before next request", waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Rate limit wait interrupted", e);
                }
            }

            lastRequestTime = System.currentTimeMillis();
        } finally {
            rateLimitLock.unlock();
        }
    }

    /**
     * Get list of available runtimes from Piston
     */
    public List<PistonRuntimeResponse> getRuntimes() {
        try {
            log.info("Fetching available runtimes from Piston API");
            enforceRateLimit();
            String url = pistonApiUrl + "/runtimes";

            ResponseEntity<List<PistonRuntimeResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PistonRuntimeResponse>>() {
                    }
            );

            log.info("Successfully fetched {} runtimes", response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching runtimes from Piston API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch runtimes: " + e.getMessage(), e);
        }
    }

    /**
     * Execute code using Piston API
     */
    public RunCodeResponse executeCode(RunCodeRequest request) {
        try {
            log.info("Executing {} code via Piston API", request.getLanguage());

            // Enforce rate limiting
            enforceRateLimit();

            // Prepare the request payload for Piston
            Map<String, Object> pistonRequest = new HashMap<>();
            pistonRequest.put("language", mapLanguageToPiston(request.getLanguage()));

            // Version is REQUIRED by Piston API - use provided or default to "*" (latest)
            String version = (request.getVersion() != null && !request.getVersion().isEmpty())
                    ? request.getVersion()
                    : "*";
            pistonRequest.put("version", version);

            // Create files array with the code
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> file = new HashMap<>();
            file.put("name", getDefaultFileName(request.getLanguage()));
            file.put("content", request.getCode());
            files.add(file);
            pistonRequest.put("files", files);

            // Add optional parameters
            if (request.getStdin() != null) {
                pistonRequest.put("stdin", request.getStdin());
            } else {
                pistonRequest.put("stdin", "");
            }
            if (request.getArgs() != null && !request.getArgs().isEmpty()) {
                pistonRequest.put("args", request.getArgs());
            }
            if (request.getRunTimeout() != null) {
                pistonRequest.put("run_timeout", request.getRunTimeout());
            } else {
                pistonRequest.put("run_timeout", 3000); // Default 3 seconds
            }
            if (request.getCompileTimeout() != null) {
                pistonRequest.put("compile_timeout", request.getCompileTimeout());
            } else {
                pistonRequest.put("compile_timeout", 10000); // Default 10 seconds
            }
            if (request.getCompileMemoryLimit() != null) {
                pistonRequest.put("compile_memory_limit", request.getCompileMemoryLimit());
            } else {
                pistonRequest.put("compile_memory_limit", -1); // No limit
            }
            if (request.getRunMemoryLimit() != null) {
                pistonRequest.put("run_memory_limit", request.getRunMemoryLimit());
            } else {
                pistonRequest.put("run_memory_limit", -1); // No limit
            }

            log.debug("Piston request: {}", pistonRequest);

            // Make the API call
            String url = pistonApiUrl + "/execute";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(pistonRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Parse the response
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from Piston API");
            }

            log.info("Code execution completed. Language: {}, Version: {}",
                    responseBody.get("language"), responseBody.get("version"));

            return mapPistonResponse(responseBody);

        } catch (Exception e) {
            log.error("Error executing code via Piston API: {}", e.getMessage(), e);

            // Return error response
            RunCodeResponse errorResponse = new RunCodeResponse();
            errorResponse.setMessage("Execution failed: " + e.getMessage());

            RunCodeResponse.RunResult runResult = new RunCodeResponse.RunResult();
            runResult.setStderr(e.getMessage());
            runResult.setCode(1);
            errorResponse.setRun(runResult);

            return errorResponse;
        }
    }

    /**
     * Map language names to Piston-compatible names
     */
    private String mapLanguageToPiston(String language) {
        Map<String, String> languageMap = new HashMap<>();
        languageMap.put("javascript", "javascript");
        languageMap.put("js", "javascript");
        languageMap.put("python", "python");
        languageMap.put("py", "python");
        languageMap.put("java", "java");
        languageMap.put("cpp", "c++");
        languageMap.put("c++", "c++");
        languageMap.put("c", "c");
        languageMap.put("csharp", "csharp");
        languageMap.put("c#", "csharp");
        languageMap.put("go", "go");
        languageMap.put("rust", "rust");
        languageMap.put("ruby", "ruby");
        languageMap.put("php", "php");
        languageMap.put("typescript", "typescript");
        languageMap.put("ts", "typescript");
        languageMap.put("kotlin", "kotlin");
        languageMap.put("swift", "swift");

        return languageMap.getOrDefault(language.toLowerCase(), language.toLowerCase());
    }

    /**
     * Get default file name based on language
     */
    private String getDefaultFileName(String language) {
        Map<String, String> fileNames = new HashMap<>();
        fileNames.put("javascript", "main.js");
        fileNames.put("js", "main.js");
        fileNames.put("python", "main.py");
        fileNames.put("py", "main.py");
        fileNames.put("java", "Main.java");
        fileNames.put("cpp", "main.cpp");
        fileNames.put("c++", "main.cpp");
        fileNames.put("c", "main.c");
        fileNames.put("csharp", "main.cs");
        fileNames.put("c#", "main.cs");
        fileNames.put("go", "main.go");
        fileNames.put("rust", "main.rs");
        fileNames.put("ruby", "main.rb");
        fileNames.put("php", "main.php");
        fileNames.put("typescript", "main.ts");
        fileNames.put("ts", "main.ts");
        fileNames.put("kotlin", "main.kt");
        fileNames.put("swift", "main.swift");

        return fileNames.getOrDefault(language.toLowerCase(), "main.txt");
    }

    /**
     * Map Piston API response to our response DTO
     */
    private RunCodeResponse mapPistonResponse(Map<String, Object> pistonResponse) {
        RunCodeResponse response = new RunCodeResponse();
        response.setLanguage((String) pistonResponse.get("language"));
        response.setVersion((String) pistonResponse.get("version"));
        response.setMessage((String) pistonResponse.get("message"));

        // Map run results
        if (pistonResponse.containsKey("run")) {
            Map<String, Object> runData = (Map<String, Object>) pistonResponse.get("run");
            RunCodeResponse.RunResult runResult = new RunCodeResponse.RunResult();
            runResult.setStdout((String) runData.get("stdout"));
            runResult.setStderr((String) runData.get("stderr"));
            runResult.setOutput((String) runData.get("output"));
            runResult.setCode((Integer) runData.get("code"));
            runResult.setSignal((String) runData.get("signal"));
            response.setRun(runResult);
        }

        // Map compile results (if present)
        if (pistonResponse.containsKey("compile")) {
            Map<String, Object> compileData = (Map<String, Object>) pistonResponse.get("compile");
            RunCodeResponse.CompileResult compileResult = new RunCodeResponse.CompileResult();
            compileResult.setStdout((String) compileData.get("stdout"));
            compileResult.setStderr((String) compileData.get("stderr"));
            compileResult.setOutput((String) compileData.get("output"));
            compileResult.setCode((Integer) compileData.get("code"));
            compileResult.setSignal((String) compileData.get("signal"));
            response.setCompile(compileResult);
        }

        return response;
    }
}
