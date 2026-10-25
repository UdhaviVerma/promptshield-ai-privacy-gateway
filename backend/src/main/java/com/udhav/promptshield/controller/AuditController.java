package com.udhav.promptshield.controller;

import com.udhav.promptshield.api.AuditEntryView;
import com.udhav.promptshield.api.AuditSummaryView;
import com.udhav.promptshield.api.DeleteAuditResponse;
import com.udhav.promptshield.service.AuditService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEntryView> list() {
        return auditService.listLatest();
    }

    @GetMapping("/summary")
    public AuditSummaryView summary() {
        return auditService.summary();
    }

    @DeleteMapping
    public DeleteAuditResponse deleteOlderThan(@RequestParam(required = false) Integer olderThanDays) {
        return new DeleteAuditResponse(auditService.deleteOlderThan(olderThanDays));
    }
}
