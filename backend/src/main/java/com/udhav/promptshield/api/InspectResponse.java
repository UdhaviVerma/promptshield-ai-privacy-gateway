package com.udhav.promptshield.api;

import com.udhav.promptshield.domain.PolicyAction;
import com.udhav.promptshield.domain.ProviderMode;

import java.util.List;

public record InspectResponse(
        String decision,
        PolicyAction finalAction,
        boolean blocked,
        String originalPrompt,
        String maskedPrompt,
        String forwardedPrompt,
        ProviderMode mode,
        String providerResponse,
        List<FindingView> findings,
        long processingMs
) {
    public record FindingView(
            String category,
            String ruleId,
            PolicyAction action,
            String explanation,
            String maskedValue
    ) {
    }
}
