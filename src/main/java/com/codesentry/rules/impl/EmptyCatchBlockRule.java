package com.codesentry.rules.impl;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;

import java.util.ArrayList;
import java.util.List;

public class EmptyCatchBlockRule implements Rule {

    @Override
    public String getRuleId() {
        return "EMPTY_CATCH_BLOCK";
    }

    @Override
    public List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines) {
        List<CodeIssue> issues = new ArrayList<>();

        List<CatchClause> catchClauses = cu.findAll(CatchClause.class);

        for (CatchClause catchClause : catchClauses) {
            boolean isEmpty = catchClause.getBody().getStatements().isEmpty();

            if (isEmpty) {
                int line = catchClause.getBegin().map(p -> p.line).orElse(0);
                String exceptionType = catchClause.getParameter().getType().asString();

                issues.add(CodeIssue.builder()
                        .ruleId(getRuleId())
                        .title("Empty catch block")
                        .description("Catch block for '" + exceptionType + "' is empty. " +
                                "Exception is being silently swallowed.")
                        .severity(Severity.HIGH)
                        .category(IssueCategory.BUG_RISK)
                        .line(line)
                        .location(catchClause
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
