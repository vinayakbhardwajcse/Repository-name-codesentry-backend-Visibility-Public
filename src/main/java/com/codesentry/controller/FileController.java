package com.codesentry.controller;

import com.codesentry.model.ScanResult;
import com.codesentry.service.CodeAnalyzerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "https://codesentry-ui.vercel.app",
    "*"
})
public class FileController {

    private final CodeAnalyzerService analyzerService;

    public FileController(CodeAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/upload")
    public ScanResult uploadAndScan(
            @RequestParam("file") MultipartFile file) throws IOException {
        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
        return analyzerService.analyze(file.getOriginalFilename(), sourceCode);
    }

    @GetMapping("/status")
    public String status() {
        return "FileController is working!";
    }
}