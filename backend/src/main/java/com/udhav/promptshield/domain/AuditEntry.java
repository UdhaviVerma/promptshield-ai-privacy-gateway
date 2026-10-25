package com.udhav.promptshield.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_entries")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 20)
    private String finalAction;

    @Column(nullable = false)
    private boolean blocked;

    @Column(nullable = false, length = 20)
    private String mode;

    @Column(nullable = false)
    private long processingMs;

    @Column(nullable = false, length = 400)
    private String matchedRuleIds;

    @Column(nullable = false, length = 400)
    private String matchedCategories;

    @Column(nullable = false)
    private int findingCount;

    protected AuditEntry() {
    }

    public AuditEntry(
            Instant createdAt,
            String finalAction,
            boolean blocked,
            String mode,
            long processingMs,
            String matchedRuleIds,
            String matchedCategories,
            int findingCount
    ) {
        this.createdAt = createdAt;
        this.finalAction = finalAction;
        this.blocked = blocked;
        this.mode = mode;
        this.processingMs = processingMs;
        this.matchedRuleIds = matchedRuleIds;
        this.matchedCategories = matchedCategories;
        this.findingCount = findingCount;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getFinalAction() {
        return finalAction;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getMode() {
        return mode;
    }

    public long getProcessingMs() {
        return processingMs;
    }

    public String getMatchedRuleIds() {
        return matchedRuleIds;
    }

    public String getMatchedCategories() {
        return matchedCategories;
    }

    public int getFindingCount() {
        return findingCount;
    }
}
