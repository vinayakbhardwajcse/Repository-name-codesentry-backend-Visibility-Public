package com.codesentry.rules;

import com.codesentry.model.CodeIssue;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public interface Rule {

    List<CodeIssue> analyze(CompilationUnit cu, String[] sourceLines);

    String getRuleId();
}
