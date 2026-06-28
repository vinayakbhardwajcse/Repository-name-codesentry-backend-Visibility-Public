package com.codesentry.rules.impl;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MagicNumberRule implements Rule {

    private static final Set<Integer> ALLOWED_VALUES = Set.of(0, 1, -1, 2);

    @Override
    public String getRuleId() {
        return "MAGIC_NUMBER";
    }

    @Override
    public List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines) {
        List<CodeIssue> issues = new ArrayList<>();

        for (IntegerLiteralExpr literal : cu.findAll(IntegerLiteralExpr.class)) {
            if (isPartOfConstantDeclaration(literal)) continue;

            int value;
            try {
                value = literal.asNumber().intValue();
            } catch (Exception e) {
                continue;
            }

            if (ALLOWED_VALUES.contains(value)) continue;

            int line = literal.getBegin().map(p -> p.line).orElse(0);
            issues.add(CodeIssue.builder()
                    .ruleId(getRuleId())
                    .title("Magic number: " + value)
                    .description("Hardcoded value " + value + " should be a named constant.")
                    .severity(Severity.LOW)
                    .category(IssueCategory.STYLE)
                    .line(line)
                    .location(literal
                            .findAncestor(com.github.javaparser.ast.body.MethodDeclaration.class)
                            .map(m -> m.getNameAsString())
                            .orElse("field/class-level"))
                    .codeSnippet(line > 0 ? sourceLines[line - 1].trim() : "")
                    .build());
        }
        return issues;
    }

    private boolean isPartOfConstantDeclaration(IntegerLiteralExpr literal) {
        return literal.findAncestor(VariableDeclarator.class)
                .flatMap(vd -> vd.findAncestor(FieldDeclaration.class)
                        .map(fd -> fd.isStatic() && fd.isFinal()))
                .orElse(false);
    }
}