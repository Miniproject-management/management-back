package com.mini3.backend.global;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String action, Long empNo, Long targetId, String detail, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .empNo(empNo)
                .targetId(targetId)
                .detail(detail)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
