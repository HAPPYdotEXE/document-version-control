package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuditLogResponseDTO;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;
import com.project.practice.sap.repository.AuditLogRepository;
import com.project.practice.sap.service.util.DtoMapper;
import com.project.practice.sap.service.util.EntityBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final DtoMapper dtoMapper;
    private final EntityBuilder entityBuilder;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, DtoMapper dtoMapper, EntityBuilder entityBuilder) {
        this.auditLogRepository = auditLogRepository;
        this.dtoMapper = dtoMapper;
        this.entityBuilder = entityBuilder;
    }

    @Override
    public void log(User actor, AuditAction action, AuditEntityType entityType, Integer entityId) {
        auditLogRepository.save(entityBuilder.buildAuditLog(actor, action, entityType, entityId));
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
    public List<AuditLogResponseDTO> getLogsByEntityType(AuditEntityType entityType) {
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
