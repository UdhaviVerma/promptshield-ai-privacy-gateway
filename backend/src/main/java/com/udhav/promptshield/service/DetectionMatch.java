package com.udhav.promptshield.service;

import com.udhav.promptshield.domain.DataCategory;

public record DetectionMatch(
        DataCategory category,
        String value,
        int start,
        int end
) {
}
