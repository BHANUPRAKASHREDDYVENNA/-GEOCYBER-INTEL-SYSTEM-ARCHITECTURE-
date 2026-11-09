package org.geocyber.security;

import org.geocyber.domain.AuditLog;
import org.geocyber.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(String principal, String action, String resource, String details, String ipAddress) {
        AuditLog entry = new AuditLog(principal, action, resource, details, ipAddress);
        auditLogRepository.save(entry);
        log.info("[AUDIT] Principal: '{}' Action: '{}' Resource: '{}'", principal, action, resource);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
