package com.codesentry.model;

public class CodeIssue {

    private String ruleId;
    private String title;
    private String description;
    private Severity severity;
    private IssueCategory category;
    private int line;
    private String location;
    private String codeSnippet;
    private String aiExplanation;
    private String suggestedFix;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CodeIssue issue = new CodeIssue();

        public Builder ruleId(String ruleId) { issue.ruleId = ruleId; return this; }
        public Builder title(String title) { issue.title = title; return this; }
        public Builder description(String description) { issue.description = description; return this; }
        public Builder severity(Severity severity) { issue.severity = severity; return this; }
        public Builder category(IssueCategory category) { issue.category = category; return this; }
        public Builder line(int line) { issue.line = line; return this; }
        public Builder location(String location) { issue.location = location; return this; }
        public Builder codeSnippet(String codeSnippet) { issue.codeSnippet = codeSnippet; return this; }
        public Builder aiExplanation(String aiExplanation) { issue.aiExplanation = aiExplanation; return this; }
        public Builder suggestedFix(String suggestedFix) { issue.suggestedFix = suggestedFix; return this; }
        public CodeIssue build() { return issue; }
    }

    // Getters
    public String getRuleId() { return ruleId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public IssueCategory getCategory() { return category; }
    public int getLine() { return line; }
    public String getLocation() { return location; }
    public String getCodeSnippet() { return codeSnippet; }
    public String getAiExplanation() { return aiExplanation; }
    public String getSuggestedFix() { return suggestedFix; }

    // Setters
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setCategory(IssueCategory category) { this.category = category; }
    public void setLine(int line) { this.line = line; }
    public void setLocation(String location) { this.location = location; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }
}