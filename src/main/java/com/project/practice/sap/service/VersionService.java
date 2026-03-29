package com.project.practice.sap.service;

import com.project.practice.sap.dto.ApproveVersionRequest;
import com.project.practice.sap.dto.VersionResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VersionService {

    VersionResponseDTO uploadNewVersion(Integer documentId, Integer userId, MultipartFile file);

    VersionResponseDTO getVersion(Integer documentId, Integer versionNum);

    List<VersionResponseDTO> getVersionHistory(Integer documentId);

    VersionResponseDTO getActiveVersion(Integer documentId);

    Resource downloadFile(Integer documentId, Integer versionNum);

    VersionResponseDTO approveVersion(Integer documentId, Integer versionNum, ApproveVersionRequest request);

    VersionResponseDTO rejectVersion(Integer documentId, Integer versionNum, ApproveVersionRequest request);
}
