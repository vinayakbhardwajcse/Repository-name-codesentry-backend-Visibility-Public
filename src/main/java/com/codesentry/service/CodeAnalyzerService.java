package com.codesentry.service;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.ScanResult;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.codesentry.rules.impl.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CodeAnalyzerService {

    private final List<Rule> rules;
    private final JavaParser javaParser;
    private final LLMService llmService;

    public CodeAnalyzerService(LLMService llmService) {
        this.llmService = llmService;
        this.javaParser = new JavaParser();
        this.rules = List.of(
                new EmptyCatchBlockRule(),
                new UnusedVariableRule(),
                new LongMethodRule(),
                new MagicNumberRule(),
                new SqlInjectionRiskRule(),
                new ResourceLeakRule()
        );
    }

    public ScanResult analyze(String fileName, String sourceCode) {
        System.out.println("=== Starting analysis for " + fileName);

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);

        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            String errors = parseResult.getProblems().stream()
                    .map(p -> p.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Could not parse Java source: " + errors);
        }

        CompilationUnit cu = parseResult.getResult().get();
        String[] sourceLines = sourceCode.split("\n", -1);

        List<CodeIssue> allIssues = new ArrayList<>();
        for (Rule rule : rules) {
            allIssues.addAll(rule.analyze(cu, sourceLines));
        }

        System.out.println("=== Found " + allIssues.size() + " issues");

        allIssues.sort(
                Comparator.comparing((CodeIssue i) -> i.getSeverity().ordinal())
                        .thenComparing(CodeIssue::getLine)
        );

        List<CodeIssue> enrichedIssues = new ArrayList<>();
        int enrichmentCount = 0;

        for (CodeIssue issue : allIssues) {
    System.out.println("=== Enriching: " + issue.getRuleId());
    if (enrichmentCount < 10) {
        enrichedIssues.add(llmService.enrich(issue));
        enrichmentCount++;
        // Add small delay to avoid rate limiting
        try {
            Thread.sleep(500); // 1 second delay between calls
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    } else {
        enrichedIssues.add(issue);
    }
}
        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) bySeverity.put(s, 0L);
        for (CodeIssue i : enrichedIssues) bySeverity.merge(i.getSeverity(), 1L, Long::sum);

        Map<IssueCategory, Long> byCategory = new EnumMap<>(IssueCategory.class);
        for (IssueCategory c : IssueCategory.values()) byCategory.put(c, 0L);
        for (CodeIssue i : enrichedIssues) byCategory.merge(i.getCategory(), 1L, Long::sum);

        int score = 100;
        for (CodeIssue issue : enrichedIssues) {
            score -= issue.getSeverity().getScorePenalty();
        }
        score = Math.max(score, 0);

        return ScanResult.builder()
                .fileName(fileName)
                .issues(enrichedIssues)
                .healthScore(score)
                .totalLines(sourceLines.length)
                .issueCountBySeverity(bySeverity)
                .issueCountByCategory(byCategory)
                .build();
    }
}