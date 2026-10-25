package com.udhav.promptshield.service;

import com.udhav.promptshield.api.InspectRequest;
import com.udhav.promptshield.api.InspectResponse;
import com.udhav.promptshield.domain.PolicyAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InspectService {

    private final DetectionService detectionService;
    private final PolicyService policyService;
    private final MaskingService maskingService;
    private final ProviderClient providerClient;
    private final AuditService auditService;
    private final int maxPromptLength;

    public InspectService(
            DetectionService detectionService,
            PolicyService policyService,
            MaskingService maskingService,
            ProviderClient providerClient,
            AuditService auditService,
            @Value("${app.inspect.max-prompt-length:6000}") int maxPromptLength
    ) {
        this.detectionService = detectionService;
        this.policyService = policyService;
        this.maskingService = maskingService;
        this.providerClient = providerClient;
        this.auditService = auditService;
        this.maxPromptLength = maxPromptLength;
    }

    public InspectResponse inspect(InspectRequest request) {
        Instant started = Instant.now();
        if (request.prompt().length() > maxPromptLength) {
            throw new PromptshieldException(413, "PROMPT_TOO_LARGE", "Prompt exceeds maximum allowed length");
        }

        List<DetectionMatch> matches = detectionService.detect(request.prompt());
        List<ResultFinding> enriched = new ArrayList<>();
        for (DetectionMatch match : matches) {
            PolicyAction action = policyService.actionForCategory(match.category());
            String ruleId = policyService.ruleIdForCategory(match.category());
            enriched.add(new ResultFinding(match, action, ruleId));
        }

        PolicyAction finalAction = decideFinalAction(enriched);
        String maskedPrompt = applyMasks(request.prompt(), enriched);
        boolean blocked = finalAction == PolicyAction.BLOCK;
        String forwardedPrompt = blocked ? null : switch (finalAction) {
            case ALLOW -> request.prompt();
            case MASK -> maskedPrompt;
            case BLOCK -> null;
        };

        String providerResponse = blocked
                ? "Blocked by PromptShield policy. No provider call attempted."
                : providerClient.respond(forwardedPrompt, request.mode());

        long processingMs = Duration.between(started, Instant.now()).toMillis();

        List<InspectResponse.FindingView> findingViews = enriched.stream()
                .map(f -> new InspectResponse.FindingView(
                        f.match().category().name(),
                        f.ruleId(),
                        f.action(),
                        explanationFor(f.action()),
                        maskingService.maskValue(f.match().category(), f.match().value())
                ))
                .toList();

        List<String> ruleIds = enriched.stream().map(ResultFinding::ruleId).distinct().toList();
        List<String> categories = enriched.stream().map(f -> f.match().category().name()).distinct().toList();
        auditService.save(finalAction, blocked, request.mode(), processingMs, ruleIds, categories);

        String decision = blocked
                ? "Request blocked. One or more matched rules require blocking before provider forwarding."
                : finalAction == PolicyAction.MASK
                ? "Request allowed with masking. Sensitive matches were redacted before forwarding."
                : "Request allowed unchanged by current policy rules.";

        return new InspectResponse(
                decision,
                finalAction,
                blocked,
                request.prompt(),
                maskedPrompt,
                forwardedPrompt,
                request.mode(),
                providerResponse,
                findingViews,
                processingMs
        );
    }

    private PolicyAction decideFinalAction(List<ResultFinding> findings) {
        if (findings.stream().anyMatch(f -> f.action() == PolicyAction.BLOCK)) {
            return PolicyAction.BLOCK;
        }
        if (findings.stream().anyMatch(f -> f.action() == PolicyAction.MASK)) {
            return PolicyAction.MASK;
        }
        return PolicyAction.ALLOW;
    }

    private String applyMasks(String prompt, List<ResultFinding> findings) {
        String updated = prompt;
        int offset = 0;
        List<ResultFinding> toMask = findings.stream()
                .filter(f -> f.action() == PolicyAction.MASK || f.action() == PolicyAction.BLOCK)
                .sorted(Comparator.comparingInt(f -> f.match().start()))
                .toList();

        for (ResultFinding finding : toMask) {
            DetectionMatch match = finding.match();
            int start = match.start() + offset;
            int end = match.end() + offset;
            String replacement = maskingService.maskValue(match.category(), match.value());
            updated = updated.substring(0, start) + replacement + updated.substring(end);
            offset += replacement.length() - (end - start);
        }
        return updated;
    }

    private String explanationFor(PolicyAction action) {
        return switch (action) {
            case ALLOW -> "Rule allows forwarding without transformation.";
            case MASK -> "Rule requires masking before provider forwarding.";
            case BLOCK -> "Rule blocks forwarding due to sensitive match.";
        };
    }

    private record ResultFinding(DetectionMatch match, PolicyAction action, String ruleId) {
    }
}
