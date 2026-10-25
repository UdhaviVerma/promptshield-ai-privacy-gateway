package com.udhav.promptshield.repository;

import com.udhav.promptshield.domain.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findTop100ByOrderByCreatedAtDesc();

    long countByFinalAction(String action);

    long deleteByCreatedAtBefore(Instant cutoff);
}
