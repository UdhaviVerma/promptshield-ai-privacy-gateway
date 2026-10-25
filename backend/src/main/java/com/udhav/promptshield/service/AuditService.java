package com.udhav.promptshield.service;

import com.udhav.promptshield.api.AuditEntryView;
import com.udhav.promptshield.api.AuditSummaryView;
import com.udhav.promptshield.domain.AuditEntry;
import com.udhav.promptshield.domain.PolicyAction;
import com.udhav.promptshield.domain.ProviderMode;
import com.udhav.promptshield.repository.AuditEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class AuditService {

    private final AuditEntryRepository auditEntryRepository;
    private final int defaultRetentionDays;

    public AuditService(AuditEntryRepository auditEntryRepository, @Value("${app.audit.retention-days:30}") int defaultRetentionDays) {
        this.auditEntryRepository = auditEntryRepository;
        this.defaultRetentionDays = defaultRetentionDays;
    }

    @Transactional
    public void save(PolicyAction finalAction, boolean blocked, ProviderMode mode, long processingMs, List<String> ruleIds, List<String> categories) {
        AuditEntry entry = new AuditEntry(
                Instant.now(),
                finalAction.name(),
                blocked,
                mode.name(),
                processingMs,
                String.join(",", ruleIds),
                String.join(",", categories),
                categories.size()
        );
        auditEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditEntryView> listLatest() {
        return auditEntryRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditSummaryView summary() {
        long allow = auditEntryRepository.countByFinalAction(PolicyAction.ALLOW.name());
        long mask = auditEntryRepository.countByFinalAction(PolicyAction.MASK.name());
        long block = auditEntryRepository.countByFinalAction(PolicyAction.BLOCK.name());
        return new AuditSummaryView(allow + mask + block, allow, mask, block);
    }

    @Transactional
    public long deleteOlderThan(Integer olderThanDays) {
        int days = olderThanDays == null ? defaultRetentionDays : olderThanDays;
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        return auditEntryRepository.deleteByCreatedAtBefore(cutoff);
    }

    private AuditEntryView toView(AuditEntry entry) {
        List<String> ruleIds = splitCsv(entry.getMatchedRuleIds());
        List<String> categories = splitCsv(entry.getMatchedCategories());
        return new AuditEntryView(
                entry.getId(),
                entry.getCreatedAt(),
                entry.getFinalAction(),
                entry.isBlocked(),
                entry.getMode(),
                entry.getProcessingMs(),
                entry.getFindingCount(),
                ruleIds,
                categories
        );
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .filter(s -> !s.isBlank())
                .toList();
    }
}
