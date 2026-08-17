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
            return buildSyntaxErrorResult(fileName, sourceCode, parseResult);
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

        List<CodeIssue> enrichedIssues = enrichIssues(allIssues);

        Map<Severity, Long> bySeverity = countBySeverity(enrichedIssues);
        Map<IssueCategory, Long> byCategory = countByCategory(enrichedIssues);

        int score = computeHealthScore(enrichedIssues);

        return ScanResult.builder()
                .fileName(fileName)
                .issues(enrichedIssues)
                .healthScore(score)
                .totalLines(sourceLines.length)
                .issueCountBySeverity(bySeverity)
                .issueCountByCategory(byCategory)
                .build();
    }

    /**
     * When JavaParser fails to parse the source (syntax error), instead of
     * throwing a generic exception, we return a normal ScanResult containing
     * a single "Syntax Error" issue with the real parser message - so the
     * API always responds with a helpful, specific explanation rather than
     * a bare failure.
     */
    private ScanResult buildSyntaxErrorResult(String fileName, String sourceCode,
                                               ParseResult<CompilationUnit> parseResult) {
        List<CodeIssue> syntaxIssues = new ArrayList<>();
        String[] lines = sourceCode.split("\\R", -1);

        parseResult.getProblems().forEach(problem -> {
            int line = problem.getLocation()
        .map(location -> location.getBegin().getRange().map(r -> r.begin.line).orElse(1))
        .orElse(1);
            String message = problem.getMessage();
            String snippet = (lines.length >= line) ? lines[line - 1] : "";

            CodeIssue issue = CodeIssue.builder()
                    .ruleId("JAVA-PARSER")
                    .title("Syntax Error")
                    .description(message)
                    .severity(Severity.HIGH)
                    .category(IssueCategory.BUG_RISK)
                    .line(line)
                    .location("Line " + line)
                    .codeSnippet(snippet)
                    .aiExplanation("The Java source code contains a syntax error that prevents it from being parsed correctly: " + message)
                    .suggestedFix("Correct the syntax error described above and scan the code again.")
                    .build();

            syntaxIssues.add(issue);
        });

        int syntaxScore = Math.max(
                0,
                100 - syntaxIssues.stream()
                        .mapToInt(issue -> issue.getSeverity().getScorePenalty())
                        .sum()
        );

        return ScanResult.builder()
                .fileName(fileName)
                .issues(syntaxIssues)
                .healthScore(syntaxScore)
                .totalLines(lines.length)
                .issueCountBySeverity(countBySeverity(syntaxIssues))
                .issueCountByCategory(countByCategory(syntaxIssues))
                .build();
    }

    private List<CodeIssue> enrichIssues(List<CodeIssue> allIssues) {
        List<CodeIssue> enrichedIssues = new ArrayList<>();
        int enrichmentCount = 0;

        for (CodeIssue issue : allIssues) {
            System.out.println("=== Enriching: " + issue.getRuleId());
            if (enrichmentCount < 10) {
                enrichedIssues.add(llmService.enrich(issue));
                enrichmentCount++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                enrichedIssues.add(issue);
            }
        }
        return enrichedIssues;
    }

    private int computeHealthScore(List<CodeIssue> issues) {
        int score = 100;
        for (CodeIssue issue : issues) {
            score -= issue.getSeverity().getScorePenalty();
        }
        return Math.max(score, 0);
    }

    private Map<Severity, Long> countBySeverity(List<CodeIssue> issues) {
        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) bySeverity.put(s, 0L);
        for (CodeIssue i : issues) bySeverity.merge(i.getSeverity(), 1L, Long::sum);
        return bySeverity;
    }

    private Map<IssueCategory, Long> countByCategory(List<CodeIssue> issues) {
        Map<IssueCategory, Long> byCategory = new EnumMap<>(IssueCategory.class);
        for (IssueCategory c : IssueCategory.values()) byCategory.put(c, 0L);
        for (CodeIssue i : issues) byCategory.merge(i.getCategory(), 1L, Long::sum);
        return byCategory;
    }
}