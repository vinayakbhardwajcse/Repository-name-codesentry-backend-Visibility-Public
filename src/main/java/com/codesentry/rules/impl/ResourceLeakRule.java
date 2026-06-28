package com.codesentry.rules.impl;

import com.codesentry.model.CodeIssue;
import com.codesentry.model.IssueCategory;
import com.codesentry.model.Severity;
import com.codesentry.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.TryStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResourceLeakRule implements Rule {

    private static final Set<String> CLOSEABLE_TYPES = Set.of(
            "FileInputStream", "FileOutputStream", "FileReader", "FileWriter",
            "BufferedReader", "BufferedWriter", "Scanner",
            "Connection", "Statement", "PreparedStatement", "ResultSet",
            "Socket", "ServerSocket"
    );

    @Override
    public String getRuleId() {
        return "RESOURCE_LEAK";
    }

    @Override
    public List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines) {
        List<CodeIssue> issues = new ArrayList<>();

        for (ObjectCreationExpr creation : cu.findAll(ObjectCreationExpr.class)) {
            String typeName = creation.getType().getNameAsString();
            if (!CLOSEABLE_TYPES.contains(typeName)) continue;
            if (isInsideTryWithResources(creation)) continue;

            int line = creation.getBegin().map(p -> p.line).orElse(0);
            issues.add(CodeIssue.builder()
                    .ruleId(getRuleId())
                    .title("Possible resource leak: " + typeName)
                    .description(typeName + " created without try-with-resources. May cause resource leak.")
                    .severity(Severity.HIGH)
                    .category(IssueCategory.BUG_RISK)
                    .line(line)
                    .location(creation
                            .findAncestor(com.github.javaparser.ast.body.MethodDeclaration.class)
                            .map(m -> m.getNameAsString())
                            .orElse("unknown"))
                    .codeSnippet(line > 0 ? sourceLines[line - 1].trim() : "")
                    .build());
        }
        return issues;
    }

    private boolean isInsideTryWithResources(ObjectCreationExpr creation) {
        return creation.findAncestor(VariableDeclarationExpr.class)
                .flatMap(varDecl -> varDecl.findAncestor(TryStmt.class)
                        .map(tryStmt -> tryStmt.getResources().contains(varDecl)))
                .orElse(false);
    }
}
