package com.udhav.promptshield.controller;

import com.udhav.promptshield.api.PolicyRuleView;
import com.udhav.promptshield.api.UpdatePolicyRequest;
import com.udhav.promptshield.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public List<PolicyRuleView> getRules() {
        return policyService.allRules();
    }

    @PutMapping("/{ruleId}")
    public PolicyRuleView updateRule(@PathVariable String ruleId, @Valid @RequestBody UpdatePolicyRequest request) {
        return policyService.updateAction(ruleId, request.action());
    }
}
