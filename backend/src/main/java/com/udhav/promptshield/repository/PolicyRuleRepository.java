package com.udhav.promptshield.repository;

import com.udhav.promptshield.domain.DataCategory;
import com.udhav.promptshield.domain.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyRuleRepository extends JpaRepository<PolicyRule, String> {
    Optional<PolicyRule> findByCategory(DataCategory category);
}
