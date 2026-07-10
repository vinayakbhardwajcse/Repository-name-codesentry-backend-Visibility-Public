package com.codesentry.controller;

import com.codesentry.service.GitHubScannerService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
public class GitHubController {

    private final GitHubScannerService gitHubScannerService;

    public GitHubController(GitHubScannerService gitHubScannerService) {
        this.gitHubScannerService = gitHubScannerService;
    }

    /**
     * Scan all Java files in a GitHub repository
     * POST http://localhost:9090/api/github/scan
     * Body: { "repoUrl": "https://github.com/username/repo" }
     */
    @PostMapping("/scan")
    public Map<String, Object> scanRepository(@RequestBody RepoRequest request) throws Exception {
        return gitHubScannerService.scanRepository(request.repoUrl());
    }

    @GetMapping("/status")
    public String status() {
        return "GitHub Scanner is ready!";
    }

    public record RepoRequest(String repoUrl) {}
}