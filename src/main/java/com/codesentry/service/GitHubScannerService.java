package com.codesentry.service;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.ScanResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class GitHubScannerService {

    @Value("${github.token}")
    private String githubToken;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CodeAnalyzerService analyzerService;

    public GitHubScannerService(CodeAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Scans all Java files in a GitHub repository
     * @param repoUrl full GitHub URL e.g. https://github.com/username/repo
     * @return list of ScanResults for each Java file found
     */
    public Map<String, Object> scanRepository(String repoUrl) throws Exception {
        // Extract owner and repo name from URL
        String[] parts = repoUrl.replace("https://github.com/", "").split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid GitHub URL format!");
        }

        String owner = parts[0];
        String repo = parts[1].replace(".git", "");

        System.out.println("=== Scanning GitHub repo: " + owner + "/" + repo);

        // Get all Java files from repo
        List<Map<String, String>> javaFiles = getJavaFiles(owner, repo, "");

        System.out.println("=== Found " + javaFiles.size() + " Java files");

        if (javaFiles.isEmpty()) {
            throw new IllegalArgumentException("No Java files found in this repository!");
        }

        // Limit to 10 files to avoid timeout
        List<Map<String, String>> filesToScan = javaFiles.subList(0, Math.min(10, javaFiles.size()));

        // Scan each file
        List<ScanResult> results = new ArrayList<>();
        int totalIssues = 0;
        int totalScore = 0;

        for (Map<String, String> file : filesToScan) {
            try {
                String content = getFileContent(file.get("url"));
                ScanResult result = analyzerService.analyze(file.get("name"), content);
                results.add(result);
                totalIssues += result.getIssues().size();
                totalScore += result.getHealthScore();
                System.out.println("=== Scanned: " + file.get("name") + " → " + result.getHealthScore() + "/100");
            } catch (Exception e) {
                System.err.println("Failed to scan " + file.get("name") + ": " + e.getMessage());
            }
        }

        // Calculate overall project health
        int overallScore = results.isEmpty() ? 0 : totalScore / results.size();

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("repoUrl", repoUrl);
        response.put("repoName", owner + "/" + repo);
        response.put("totalFilesScanned", results.size());
        response.put("totalJavaFilesFound", javaFiles.size());
        response.put("totalIssues", totalIssues);
        response.put("overallHealthScore", overallScore);
        response.put("fileResults", results);

        return response;
    }

    private List<Map<String, String>> getJavaFiles(String owner, String repo, String path) throws Exception {
        List<Map<String, String>> javaFiles = new ArrayList<>();

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API error: " + response.statusCode());
        }

        JsonNode items = objectMapper.readTree(response.body());

        for (JsonNode item : items) {
            String type = item.get("type").asText();
            String name = item.get("name").asText();

            if (type.equals("file") && name.endsWith(".java")) {
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("name", name);
                fileInfo.put("url", item.get("download_url").asText());
                fileInfo.put("path", item.get("path").asText());
                javaFiles.add(fileInfo);
            } else if (type.equals("dir") && javaFiles.size() < 20) {
                // Recursively scan subdirectories
                javaFiles.addAll(getJavaFiles(owner, repo, item.get("path").asText()));
            }
        }

        return javaFiles;
    }

    private String getFileContent(String downloadUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .header("Authorization", "Bearer " + githubToken)
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to download file: " + response.statusCode());
        }

        return response.body();
    }
}