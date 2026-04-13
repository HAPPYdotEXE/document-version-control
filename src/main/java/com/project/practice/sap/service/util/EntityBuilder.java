package com.project.practice.sap.service.util;

import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
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
}
