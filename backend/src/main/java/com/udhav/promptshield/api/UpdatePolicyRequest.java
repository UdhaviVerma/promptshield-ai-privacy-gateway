package com.udhav.promptshield.api;

import com.udhav.promptshield.domain.PolicyAction;
import jakarta.validation.constraints.NotNull;

public record UpdatePolicyRequest(
        @NotNull(message = "action is required")
        PolicyAction action
) {
}
