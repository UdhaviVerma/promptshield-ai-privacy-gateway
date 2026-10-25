package com.udhav.promptshield.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "policy_rules")
public class PolicyRule {

    @Id
    @Column(length = 80)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DataCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyAction action;

    @Column(nullable = false, length = 200)
    private String description;

    protected PolicyRule() {
    }

    public PolicyRule(String id, DataCategory category, PolicyAction action, String description) {
        this.id = id;
        this.category = category;
        this.action = action;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public DataCategory getCategory() {
        return category;
    }

    public PolicyAction getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public void setAction(PolicyAction action) {
        this.action = action;
    }
}
