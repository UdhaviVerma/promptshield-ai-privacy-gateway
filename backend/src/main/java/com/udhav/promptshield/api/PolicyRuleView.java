package com.udhav.promptshield.api;

import com.udhav.promptshield.domain.DataCategory;
import com.udhav.promptshield.domain.PolicyAction;

public record PolicyRuleView(
        String id,
        DataCategory category,
        PolicyAction action,
        String description
) {
}
