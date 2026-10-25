package com.udhav.promptshield.api;

public record AuditSummaryView(
        long total,
        long allowCount,
        long maskCount,
        long blockCount
) {
}
