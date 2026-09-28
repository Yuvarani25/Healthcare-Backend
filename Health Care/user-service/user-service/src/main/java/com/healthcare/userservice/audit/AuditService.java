package com.healthcare.userservice.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /** Written in its own transaction so the trail survives a rollback of the audited operation. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, AuditAction action, AuditOutcome outcome, String details) {
        AuditLog entry = AuditLog.builder()
                .username(username)
                .action(action)
                .outcome(outcome)
                .details(details)
                .ipAddress(currentClientIp())
                .build();
        auditLogRepository.save(entry);
        log.info("AUDIT {} {} user={} ip={} details={}", action, outcome, username, entry.getIpAddress(), details);
    }

    private String currentClientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return null;
    }
}
