package com.codesentry.model;

import java.util.List;
import java.util.Map;

public class ScanResult {

    private String fileName;
    private List<CodeIssue> issues;
    private int healthScore;
    private int totalLines;
    private Map<Severity, Long> issueCountBySeverity;
    private Map<IssueCategory, Long> issueCountByCategory;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ScanResult result = new ScanResult();

        public Builder fileName(String fileName) { result.fileName = fileName; return this; }
        public Builder issues(List<CodeIssue> issues) { result.issues = issues; return this; }
        public Builder healthScore(int healthScore) { result.healthScore = healthScore; return this; }
        public Builder totalLines(int totalLines) { result.totalLines = totalLines; return this; }
        public Builder issueCountBySeverity(Map<Severity, Long> map) { result.issueCountBySeverity = map; return this; }
        public Builder issueCountByCategory(Map<IssueCategory, Long> map) { result.issueCountByCategory = map; return this; }
        public ScanResult build() { return result; }
    }

    // Getters
    public String getFileName() { return fileName; }
    public List<CodeIssue> getIssues() { return issues; }
    public int getHealthScore() { return healthScore; }
    public int getTotalLines() { return totalLines; }
    public Map<Severity, Long> getIssueCountBySeverity() { return issueCountBySeverity; }
    public Map<IssueCategory, Long> getIssueCountByCategory() { return issueCountByCategory; }

    // Setters
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setIssues(List<CodeIssue> issues) { this.issues = issues; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
    public void setTotalLines(int totalLines) { this.totalLines = totalLines; }
    public void setIssueCountBySeverity(Map<Severity, Long> map) { this.issueCountBySeverity = map; }
    public void setIssueCountByCategory(Map<IssueCategory, Long> map) { this.issueCountByCategory = map; }
}
