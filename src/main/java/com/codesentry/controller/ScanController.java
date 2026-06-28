package com.codesentry.controller;

import com.codesentry.model.ScanResult;
import com.codesentry.service.CodeAnalyzerService;
import com.codesentry.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/scan")
@CrossOrigin(origins = "*")
public class ScanController {

    private final CodeAnalyzerService analyzerService;
    private final PdfReportService pdfReportService;

    public ScanController(CodeAnalyzerService analyzerService,
                          PdfReportService pdfReportService) {
        this.analyzerService = analyzerService;
        this.pdfReportService = pdfReportService;
    }

    // POST http://localhost:8080/api/scan/text
    @PostMapping("/text")
    public ScanResult scanText(@RequestBody ScanRequest request) {
        return analyzerService.analyze(request.fileName(), request.sourceCode());
    }

    // POST http://localhost:8080/api/scan/file
    @PostMapping("/file")
    public ScanResult scanFile(@RequestParam("file") MultipartFile file) throws IOException {
        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
        return analyzerService.analyze(file.getOriginalFilename(), sourceCode);
    }

    // POST http://localhost:8080/api/scan/report
    @PostMapping("/report")
    public ResponseEntity<byte[]> scanAndDownloadReport(
            @RequestBody ScanRequest request) {

        ScanResult result = analyzerService.analyze(
                request.fileName(), request.sourceCode());

        byte[] pdfBytes = pdfReportService.generateReport(result);

        String filename = request.fileName()
                .replace(".java", "")
                + "_codesentry_report.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // GET http://localhost:8080/api/scan/status
    @GetMapping("/status")
    public String status() {
        return "FileController is working";
    }

    public record ScanRequest(String fileName, String sourceCode) {}
}