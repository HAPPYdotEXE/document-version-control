package com.project.practice.sap.service;

import java.util.List;

import com.project.practice.sap.dto.CreateVersionRequestDTO;
import com.project.practice.sap.dto.ReviewVersionRequestDTO;
import com.project.practice.sap.dto.VersionResponseDTO;

public interface VersionService {
    VersionResponseDTO createVersion(Long documentId, CreateVersionRequestDTO request);
    List<VersionResponseDTO> getVersionsByDocumentId(Long documentId);
    VersionResponseDTO getVersionById(Long versionId);
    VersionResponseDTO approveVersion(Long versionId, ReviewVersionRequestDTO request);
    VersionResponseDTO rejectVersion(Long versionId, ReviewVersionRequestDTO request);
    VersionResponseDTO activateVersion(Long versionId);
}
