package com.udhav.promptshield.api;

import java.time.Instant;
import java.util.List;

public record AuditEntryView(
        Long id,
        Instant timestamp,
        String finalAction,
        boolean blocked,
        String mode,
        long processingMs,
        int findingCount,
        List<String> matchedRuleIds,
        List<String> matchedCategories
) {
}
