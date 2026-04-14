package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuditLogResponseDTO;
import com.project.practice.sap.model.AuditLog;
import com.project.practice.sap.model.User;
import com.project.practice.sap.repository.AuditLogRepository;
import com.project.practice.sap.service.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final DtoMapper dtoMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, DtoMapper dtoMapper) {
        this.auditLogRepository = auditLogRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void log(User actor, String action, String entityType, Integer entityId) {
        AuditLog entry = new AuditLog();
        entry.setPerformedBy(actor);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        auditLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAllLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(log -> dtoMapper.toAuditLogDTO(log))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType)
                .stream()
                .map(log -> dtoMapper.toAuditLogDTO(log))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getLogsByUser(Integer userId) {
        return auditLogRepository.findByPerformedById(userId)
                .stream()
                .map(log -> dtoMapper.toAuditLogDTO(log))
                .toList();
    }
}
