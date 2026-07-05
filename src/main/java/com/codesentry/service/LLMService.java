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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try {
            System.out.println("=== LLMService: Enriching " + issue.getRuleId());
            String prompt = buildPrompt(issue);
            String responseJson = callGemini(prompt);
            String rawText = extractTextFromResponse(responseJson);
            return parseEnrichedIssue(issue, rawText);
        } catch (Exception e) {
            System.err.println("[LLMService] Enrichment failed: " + e.getMessage());
            issue.setAiExplanation("This is a " + issue.getSeverity()
                    + " severity issue. " + issue.getDescription());
            issue.setSuggestedFix("// Fix for " + issue.getRuleId()
                    + "\n// Follow Java best practices.");
            return issue;
        }
    }

    private String buildPrompt(CodeIssue issue) {
        return "You are a Java code review assistant. Analyze this code issue and respond ONLY with "
                + "a valid JSON object - no markdown, no backticks, no preamble. Just raw JSON.\n\n"
                + "Issue details:\n"
                + "- Rule: " + issue.getRuleId() + "\n"
                + "- Title: " + issue.getTitle() + "\n"
                + "- Description: " + issue.getDescription() + "\n"
                + "- Severity: " + issue.getSeverity() + "\n"
                + "- Code snippet: " + (issue.getCodeSnippet() != null ? issue.getCodeSnippet() : "not available") + "\n"
                + "- Found in method: " + (issue.getLocation() != null ? issue.getLocation() : "unknown") + "\n\n"
                + "Respond with exactly this JSON structure:\n"
                + "{\"explanation\": \"2-3 sentence plain English explanation of why this is problematic\","
                + "\"suggestedFix\": \"the corrected version of the code snippet\"}";
    }

    private String callGemini(String prompt) throws Exception {
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.3);
        generationConfig.put("maxOutputTokens", 400);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("contents", List.of(content));
        requestMap.put("generationConfig", generationConfig);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        System.out.println("=== LLMService: Sending request to Gemini");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl.trim() + "?key=" + apiKey.trim()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== LLMService: Gemini responded with status " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API returned HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    private String extractTextFromResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        return root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();
    }

    private CodeIssue parseEnrichedIssue(CodeIssue issue, String rawText) throws Exception {
        String cleaned = rawText
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        JsonNode parsed = objectMapper.readTree(cleaned);

        issue.setAiExplanation(parsed.path("explanation")
                .asText("No explanation available"));
        issue.setSuggestedFix(parsed.path("suggestedFix")
                .asText("No fix suggestion available"));

        return issue;
    }
}