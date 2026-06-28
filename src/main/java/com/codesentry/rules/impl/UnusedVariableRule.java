package com.codesentry.rules.impl;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.ArrayList;
import java.util.List;

public class UnusedVariableRule implements Rule {

    @Override
    public String getRuleId() {
        return "UNUSED_VARIABLE";
    }

    @Override
    public List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines) {
        List<CodeIssue> issues = new ArrayList<>();

        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
            if (method.getBody().isEmpty()) continue;

            for (VariableDeclarator decl : method.findAll(VariableDeclarator.class)) {
                String varName = decl.getNameAsString();

                long usageCount = method.findAll(NameExpr.class).stream()
                        .filter(n -> n.getNameAsString().equals(varName))
                        .count();

                if (usageCount == 0) {
                    int line = decl.getBegin().map(p -> p.line).orElse(0);
                    issues.add(CodeIssue.builder()
                            .ruleId(getRuleId())
                            .title("Unused variable: '" + varName + "'")
                            .description("Variable '" + varName + "' is declared but never used.")
                            .severity(Severity.LOW)
                            .category(IssueCategory.CODE_SMELL)
                            .line(line)
                            .location(method.getNameAsString())
                            .codeSnippet(line > 0 ? sourceLines[line - 1].trim() : "")
                            .build());
                }
            }
        }
        return issues;
    }
}
