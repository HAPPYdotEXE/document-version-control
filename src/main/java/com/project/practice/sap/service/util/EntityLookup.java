package com.project.practice.sap.service.util;

import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;
import org.springframework.stereotype.Component;

@Component
public class EntityLookup {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final VersionRepository versionRepository;

    public EntityLookup(UserRepository userRepository,
                        DocumentRepository documentRepository,
                        VersionRepository versionRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Document findDocumentById(Integer id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    public Version findVersionByDocumentAndNum(Integer documentId, Integer versionNum) {
        return versionRepository.findByDocumentIdAndVersionNum(documentId, versionNum)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Version " + versionNum + " not found for document " + documentId));
    }
}
