package com.healthcare.userservice.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(@RequestParam(required = false) String username,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 200));
        return ResponseEntity.ok(username == null
                ? auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                : auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable));
    }
}
