package com.codesentry.rules.impl;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SqlInjectionRiskRule implements Rule {

    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)\\b(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)\\b"
    );

    @Override
    public String getRuleId() {
        return "SQL_INJECTION_RISK";
    }

    @Override
    public List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines) {
        List<CodeIssue> issues = new ArrayList<>();

        for (BinaryExpr expr : cu.findAll(BinaryExpr.class)) {
            if (expr.getOperator() != BinaryExpr.Operator.PLUS) continue;

            boolean leftIsSql = SQL_PATTERN.matcher(expr.getLeft().toString()).find();
            boolean rightIsSql = SQL_PATTERN.matcher(expr.getRight().toString()).find();
            boolean leftIsLiteral = expr.getLeft() instanceof StringLiteralExpr;
            boolean rightIsLiteral = expr.getRight() instanceof StringLiteralExpr;

            if ((leftIsSql && !rightIsLiteral) || (rightIsSql && !leftIsLiteral)) {
                int line = expr.getBegin().map(p -> p.line).orElse(0);
                issues.add(CodeIssue.builder()
                        .ruleId(getRuleId())
                        .title("Potential SQL injection")
                        .description("SQL query built via string concatenation. Use PreparedStatement instead.")
                        .severity(Severity.CRITICAL)
                        .category(IssueCategory.SECURITY)
                        .line(line)
                        .location(expr
                                .findAncestor(com.github.javaparser.ast.body.MethodDeclaration.class)
                                .map(m -> m.getNameAsString())
                                .orElse("unknown"))
                        .codeSnippet(line > 0 ? sourceLines[line - 1].trim() : "")
                        .build());
            }
        }
        return issues;
    }
}
