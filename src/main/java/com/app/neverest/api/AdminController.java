package com.app.neverest.api;

import com.app.neverest.api.dto.AuditLogResponse;
import com.app.neverest.audit.AuditLogEntry;
import com.app.neverest.audit.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuditLogService auditLogService;

    public AdminController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/audit-logs")
    public List<AuditLogResponse> getAuditLogs(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) Boolean success
    ) {
        return auditLogService.getEntries(limit, action, actor, success)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLogEntry entry) {
        return new AuditLogResponse(
                entry.id(),
                entry.timestamp(),
                entry.action(),
                entry.actor(),
                entry.success(),
                entry.message(),
                entry.metadata()
        );
    }
}
