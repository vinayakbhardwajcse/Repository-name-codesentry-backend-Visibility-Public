package com.codesentry.service;

import com.codesentry.model.CodeIssue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class LLMService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LLMService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CodeIssue enrich(CodeIssue issue) {
        System.out.println("=== LLMService: Enriching " + issue.getRuleId());

        // Mock response for development
        issue.setAiExplanation("This is a " + issue.getSeverity()
                + " severity issue. " + issue.getDescription()
                + " This pattern can lead to runtime errors and should be fixed immediately.");

        issue.setSuggestedFix("// Fix for " + issue.getRuleId()
                + "\n// Follow Java best practices to resolve this issue.");

        return issue;
    }
}