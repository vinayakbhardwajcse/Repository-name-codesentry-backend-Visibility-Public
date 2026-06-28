package com.codesentry.rules.impl;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class LongMethodRule implements Rule {

    private static final int MAX_LINES = 40;

    @Override
    public String getRuleId() {
        return "LONG_METHOD";
    }

    @Override
    public List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines) {
        List<CodeIssue> issues = new ArrayList<>();

        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
            if (method.getBegin().isEmpty() || method.getEnd().isEmpty()) continue;

            int startLine = method.getBegin().get().line;
            int endLine = method.getEnd().get().line;
            int lineCount = endLine - startLine + 1;

            if (lineCount > MAX_LINES) {
                issues.add(CodeIssue.builder()
                        .ruleId(getRuleId())
                        .title("Long method: '" + method.getNameAsString() + "' (" + lineCount + " lines)")
                        .description("Method is too long. Consider breaking it into smaller methods.")
                        .severity(lineCount > MAX_LINES * 2 ? Severity.HIGH : Severity.MEDIUM)
                        .category(IssueCategory.CODE_SMELL)
                        .line(startLine)
                        .location(method.getNameAsString())
                        .codeSnippet(sourceLines[startLine - 1].trim())
                        .build());
            }
        }
        return issues;
    }
}
