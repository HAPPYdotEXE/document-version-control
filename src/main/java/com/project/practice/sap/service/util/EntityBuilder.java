package com.project.practice.sap.service.util;

import com.project.practice.sap.model.AuditLog;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;
import com.project.practice.sap.model.enums.DocumentStatus;
import org.springframework.stereotype.Component;

@Component
public class EntityBuilder {

    public Document buildDocument(String name, User createdBy) {
        Document document = new Document();
        document.setName(name);
        document.setCreatedBy(createdBy);
        return document;
    }

    public Version buildVersion(Document document, User createdBy, int versionNum, String filePath) {
        Version version = new Version();
        version.setDocument(document);
        version.setCreatedBy(createdBy);
        version.setVersionNum(versionNum);
        version.setStatus(DocumentStatus.UNDER_REVIEW);
        version.setActive(false);
        version.setFilePath(filePath);
        return version;
    }

    public AuditLog buildAuditLog(User actor, AuditAction action, AuditEntityType entityType, Integer entityId) {
        AuditLog entry = new AuditLog();
        entry.setPerformedBy(actor);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        return entry;
    }
}
