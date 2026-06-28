package com.codesentry.model;

public enum Severity {
    CRITICAL(25),
    HIGH(15),
    MEDIUM(8),
    LOW(3);

    private final int scorePenalty;

    Severity(int scorePenalty) {
        this.scorePenalty = scorePenalty;
    }

    public int getScorePenalty() {
        return scorePenalty;
    }
}
