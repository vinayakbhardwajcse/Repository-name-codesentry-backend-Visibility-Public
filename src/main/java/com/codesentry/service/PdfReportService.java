package com.codesentry.service;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.ScanResult;
import com.codesentry.model.Severity;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    private static final DeviceRgb COLOR_PRIMARY   = new DeviceRgb(37, 99, 235);
    private static final DeviceRgb COLOR_CRITICAL  = new DeviceRgb(220, 38, 38);
    private static final DeviceRgb COLOR_HIGH      = new DeviceRgb(234, 88, 12);
    private static final DeviceRgb COLOR_MEDIUM    = new DeviceRgb(202, 138, 4);
    private static final DeviceRgb COLOR_LOW       = new DeviceRgb(22, 163, 74);
    private static final DeviceRgb COLOR_HEADER_BG = new DeviceRgb(239, 246, 255);
    private static final DeviceRgb COLOR_DARK_TEXT = new DeviceRgb(15, 23, 42);

    public byte[] generateReport(ScanResult result) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont mono    = PdfFontFactory.createFont(StandardFonts.COURIER);

            addHeader(doc, bold, result);
            addHealthScore(doc, bold, regular, result);
            addSummaryTable(doc, bold, regular, result);
            addIssuesSection(doc, bold, regular, mono, result);
            addFooter(doc, regular);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document doc, PdfFont bold, ScanResult result) throws Exception {
        doc.add(new Paragraph("CodeSentry")
                .setFont(bold).setFontSize(26)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        doc.add(new Paragraph("AI-Powered Java Code Review Report")
                .setFontSize(12).setFontColor(COLOR_DARK_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16));

        String scanTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        meta.addCell(metaCell("File", result.getFileName(), bold));
        meta.addCell(metaCell("Scanned At", scanTime, bold));
        meta.addCell(metaCell("Total Lines", String.valueOf(result.getTotalLines()), bold));
        meta.addCell(metaCell("Total Issues", String.valueOf(result.getIssues().size()), bold));
        doc.add(meta);
    }

    private void addHealthScore(Document doc, PdfFont bold, PdfFont regular, ScanResult result) {
        int score = result.getHealthScore();
        DeviceRgb scoreColor = score >= 80 ? COLOR_LOW : score >= 50 ? COLOR_MEDIUM : COLOR_CRITICAL;

        doc.add(new Paragraph("Code Health Score")
                .setFont(bold).setFontSize(14)
                .setFontColor(COLOR_DARK_TEXT)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(score + " / 100")
                .setFont(bold).setFontSize(40)
                .setFontColor(scoreColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        String verdict = score >= 80 ? "Good - minor improvements recommended"
                : score >= 50 ? "Fair - several issues need attention"
                : "Poor - critical issues require immediate fixes";

        doc.add(new Paragraph(verdict)
                .setFont(regular).setFontSize(11)
                .setFontColor(scoreColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    private void addSummaryTable(Document doc, PdfFont bold, PdfFont regular, ScanResult result) {
        doc.add(sectionHeader("Issue Summary", bold));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        table.addHeaderCell(headerCell("Severity", bold));
        table.addHeaderCell(headerCell("Count", bold));
        table.addHeaderCell(headerCell("Score Impact", bold));

        for (Severity sev : Severity.values()) {
            long count = result.getIssueCountBySeverity().getOrDefault(sev, 0L);
            int impact = (int) (count * sev.getScorePenalty());
            DeviceRgb color = severityColor(sev);

            table.addCell(new Cell().add(new Paragraph(sev.name())
                    .setFont(bold).setFontColor(color).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(count))
                    .setFont(regular).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph("-" + impact + " pts")
                    .setFont(regular).setFontSize(10)
                    .setFontColor(impact > 0 ? COLOR_CRITICAL : COLOR_LOW)
                    .setTextAlignment(TextAlignment.CENTER)));
        }
        doc.add(table);
    }

    private void addIssuesSection(Document doc, PdfFont bold, PdfFont regular,
                                   PdfFont mono, ScanResult result) {
        doc.add(sectionHeader("Detailed Issues", bold));

        if (result.getIssues().isEmpty()) {
            doc.add(new Paragraph("No issues found. This file looks clean!")
                    .setFont(bold).setFontColor(COLOR_LOW)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));
            return;
        }

        int num = 1;
        for (CodeIssue issue : result.getIssues()) {
            addIssueBlock(doc, bold, regular, mono, issue, num++);
        }
    }

    private void addIssueBlock(Document doc, PdfFont bold, PdfFont regular,
                                PdfFont mono, CodeIssue issue, int num) {
        DeviceRgb color = severityColor(issue.getSeverity());

        doc.add(new Paragraph(num + ". [" + issue.getSeverity() + "] " + issue.getTitle())
                .setFont(bold).setFontSize(11)
                .setFontColor(color)
                .setBackgroundColor(COLOR_HEADER_BG)
                .setPadding(6).setMarginTop(10).setMarginBottom(4));

        doc.add(new Paragraph("Rule: " + issue.getRuleId()
                + "   |   Line: " + issue.getLine()
                + "   |   Method: " + issue.getLocation()
                + "   |   Category: " + issue.getCategory())
                .setFont(regular).setFontSize(9)
                .setFontColor(new DeviceRgb(100, 116, 139))
                .setMarginBottom(4));

        if (issue.getCodeSnippet() != null && !issue.getCodeSnippet().isBlank()) {
            doc.add(new Paragraph("Code:")
                    .setFont(bold).setFontSize(10).setMarginBottom(2));
            doc.add(new Paragraph(issue.getCodeSnippet())
                    .setFont(mono).setFontSize(9)
                    .setBackgroundColor(new DeviceRgb(248, 250, 252))
                    .setPadding(6).setMarginBottom(6));
        }

        if (issue.getAiExplanation() != null && !issue.getAiExplanation().isBlank()
                && !issue.getAiExplanation().equals("No explanation available")) {
            doc.add(new Paragraph("AI Explanation:")
                    .setFont(bold).setFontSize(10).setMarginBottom(2));
            doc.add(new Paragraph(issue.getAiExplanation())
                    .setFont(regular).setFontSize(10).setMarginBottom(6));
        }

        if (issue.getSuggestedFix() != null && !issue.getSuggestedFix().isBlank()
                && !issue.getSuggestedFix().equals("No fix suggestion available")) {
            doc.add(new Paragraph("Suggested Fix:")
                    .setFont(bold).setFontSize(10).setMarginBottom(2));
            doc.add(new Paragraph(issue.getSuggestedFix())
                    .setFont(mono).setFontSize(9)
                    .setBackgroundColor(new DeviceRgb(240, 253, 244))
                    .setPadding(6).setMarginBottom(8));
        }
    }

    private void addFooter(Document doc, PdfFont regular) {
        doc.add(new Paragraph("Generated by CodeSentry - AI-Powered Java Code Review System")
                .setFont(regular).setFontSize(9)
                .setFontColor(new DeviceRgb(148, 163, 184))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
    }

    private Paragraph sectionHeader(String text, PdfFont bold) {
        return new Paragraph(text)
                .setFont(bold).setFontSize(14)
                .setFontColor(COLOR_PRIMARY)
                .setMarginBottom(10).setMarginTop(10);
    }

    private Cell headerCell(String text, PdfFont bold) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(bold).setFontSize(10)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARY)
                .setPadding(6);
    }

    private Cell metaCell(String label, String value, PdfFont bold) {
        return new Cell()
                .add(new Paragraph(label + ": " + value)
                        .setFont(bold).setFontSize(10))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(4);
    }

    private DeviceRgb severityColor(Severity severity) {
        return switch (severity) {
            case CRITICAL -> COLOR_CRITICAL;
            case HIGH     -> COLOR_HIGH;
            case MEDIUM   -> COLOR_MEDIUM;
            case LOW      -> COLOR_LOW;
        };
    }
}
