package com.example.demo.service;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;
    public AuditLogService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional
    public void log(String actor, String action, String targetType, Object targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? null : String.valueOf(targetId));
        log.setDetail(detail);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .limit(100)
                .map(log -> new AuditLogResponse(log.getId(), log.getActor(), log.getAction(), log.getTargetType(), log.getTargetId(), log.getDetail(), log.getCreatedAt()))
                .toList();
    }
}
