package com.udhav.promptshield.service;

import com.udhav.promptshield.api.PolicyRuleView;
import com.udhav.promptshield.domain.DataCategory;
import com.udhav.promptshield.domain.PolicyAction;
import com.udhav.promptshield.domain.PolicyRule;
import com.udhav.promptshield.repository.PolicyRuleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PolicyService {

    private final PolicyRuleRepository policyRuleRepository;

    public PolicyService(PolicyRuleRepository policyRuleRepository) {
        this.policyRuleRepository = policyRuleRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeDefaults() {
        upsertDefault("rule-email", DataCategory.EMAIL, PolicyAction.MASK, "Mask email-like values before forwarding");
        upsertDefault("rule-phone", DataCategory.PHONE, PolicyAction.MASK, "Mask phone-like values before forwarding");
        upsertDefault("rule-payment-card", DataCategory.PAYMENT_CARD, PolicyAction.BLOCK, "Block prompts containing valid payment-card-like numbers");
        upsertDefault("rule-api-key", DataCategory.API_KEY, PolicyAction.BLOCK, "Block prompts containing common API-key patterns");
    }

    private void upsertDefault(String id, DataCategory category, PolicyAction action, String description) {
        if (policyRuleRepository.findById(id).isEmpty()) {
            policyRuleRepository.save(new PolicyRule(id, category, action, description));
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyRuleView> allRules() {
        return policyRuleRepository.findAll().stream()
                .sorted(Comparator.comparing(PolicyRule::getId))
                .map(r -> new PolicyRuleView(r.getId(), r.getCategory(), r.getAction(), r.getDescription()))
                .toList();
    }

    @Transactional
    public PolicyRuleView updateAction(String ruleId, PolicyAction action) {
        PolicyRule rule = policyRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PromptshieldException(404, "RULE_NOT_FOUND", "Rule not found: " + ruleId));
        rule.setAction(action);
        policyRuleRepository.save(rule);
        return new PolicyRuleView(rule.getId(), rule.getCategory(), rule.getAction(), rule.getDescription());
    }

    @Transactional(readOnly = true)
    public PolicyAction actionForCategory(DataCategory category) {
        return policyRuleRepository.findByCategory(category)
                .map(PolicyRule::getAction)
                .orElse(PolicyAction.MASK);
    }

    @Transactional(readOnly = true)
    public String ruleIdForCategory(DataCategory category) {
        return policyRuleRepository.findByCategory(category)
                .map(PolicyRule::getId)
                .orElse("rule-unknown");
    }
}
