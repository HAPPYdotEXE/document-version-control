// package com.project.practice.sap.service;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.project.practice.sap.dto.CreateVersionRequestDTO;
// import com.project.practice.sap.dto.ReviewVersionRequestDTO;
// import com.project.practice.sap.dto.VersionResponseDTO;
// import com.project.practice.sap.model.Document;
// import com.project.practice.sap.model.User;
// import com.project.practice.sap.model.Version;
// import com.project.practice.sap.model.enums.VersionStatus;
// import com.project.practice.sap.repository.DocumentRepository;
// import com.project.practice.sap.repository.UserRepository;
// import com.project.practice.sap.repository.VersionRepository;

// @Service
// @Transactional
// public class VersionServiceImpl implements VersionService {

//     private final VersionRepository versionRepository;
//     private final DocumentRepository documentRepository;
//     private final UserRepository userRepository;

//     public VersionServiceImpl(
//             VersionRepository versionRepository,
//             DocumentRepository documentRepository,
//             UserRepository userRepository) {
//         this.versionRepository = versionRepository;
//         this.documentRepository = documentRepository;
//         this.userRepository = userRepository;
//     }

//     @Override
//     public VersionResponseDTO createVersion(Long documentId, CreateVersionRequestDTO request) {
//         Document document = documentRepository.findById(documentId)
//                 .orElseThrow(() -> new RuntimeException("Document not found"));

//         User creator = userRepository.findById(request.createdById())
//                 .orElseThrow(() -> new RuntimeException("User not found"));

//         int nextVersionNumber = versionRepository.findFirstByDocumentIdOrderByVersionNumberDesc(documentId)
//                 .map(version -> version.getVersionNumber() + 1)
//                 .orElse(1);

//         Version version = new Version();
//         version.setDocument(document);
//         version.setVersionNumber(nextVersionNumber);
//         version.setContent(request.content());
//         version.setChangeSummary(request.changeSummary());
//         version.setStatus(VersionStatus.REVIEW);
//         version.setCreatedBy(creator);

//         Version saved = versionRepository.save(version);
//         return toResponse(saved);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<VersionResponseDTO> getVersionsByDocumentId(Long documentId) {
//         return versionRepository.findByDocumentIdOrderByVersionNumberAsc(documentId)
//                 .stream()
//                 .map(this::toResponse)
//                 .toList();
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public VersionResponseDTO getVersionById(Long versionId) {
//         Version version = versionRepository.findById(versionId)
//                 .orElseThrow(() -> new RuntimeException("Version not found"));
//         return toResponse(version);
//     }

//     @Override
//     public VersionResponseDTO approveVersion(Long versionId, ReviewVersionRequestDTO request) {
//         Version version = versionRepository.findById(versionId)
//                 .orElseThrow(() -> new RuntimeException("Version not found"));

//         if (version.getStatus() != VersionStatus.REVIEW) {
//             throw new RuntimeException("Only PENDING_REVIEW versions can be approved");
//         }

//         User reviewer = userRepository.findById(request.reviewerId())
//                 .orElseThrow(() -> new RuntimeException("Reviewer not found"));

//         version.setStatus(VersionStatus.APPROVED);
//         version.setReviewedBy(reviewer);
//         version.setReviewedAt(LocalDateTime.now());
//         version.setReviewComment(request.comment());

//         return toResponse(versionRepository.save(version));
//     }

//     @Override
//     public VersionResponseDTO rejectVersion(Long versionId, ReviewVersionRequestDTO request) {
//         Version version = versionRepository.findById(versionId)
//                 .orElseThrow(() -> new RuntimeException("Version not found"));

//         if (version.getStatus() != VersionStatus.REVIEW) {
//             throw new RuntimeException("Only PENDING_REVIEW versions can be rejected");
//         }

//         User reviewer = userRepository.findById(request.reviewerId())
//                 .orElseThrow(() -> new RuntimeException("Reviewer not found"));

//         version.setStatus(VersionStatus.REJECTED);
//         version.setReviewedBy(reviewer);
//         version.setReviewedAt(LocalDateTime.now());
//         version.setReviewComment(request.comment());

//         return toResponse(versionRepository.save(version));
//     }

//     @Override
//     public VersionResponseDTO activateVersion(Long versionId) {
//         Version version = versionRepository.findById(versionId)
//                 .orElseThrow(() -> new RuntimeException("Version not found"));

//         if (version.getStatus() != VersionStatus.APPROVED) {
//             throw new RuntimeException("Only APPROVED versions can be activated");
//         }

//         Document document = version.getDocument();
//         document.setActiveVersion(version);
//         documentRepository.save(document);

//         return toResponse(version);
//     }

//     private VersionResponseDTO toResponse(Version version) {
//         String reviewedByUsername = version.getReviewedBy() != null
//                 ? version.getReviewedBy().getUsername()
//                 : null;

//         return new VersionResponseDTO(
//                 version.getId(),
//                 version.getDocument().getId(),
//                 version.getVersionNumber(),
//                 version.getContent(),
//                 version.getChangeSummary(),
//                 version.getStatus(),
//                 version.getCreatedBy().getUsername(),
//                 version.getCreatedAt(),
//                 reviewedByUsername,
//                 version.getReviewedAt(),
//                 version.getReviewComment()
//         );
//     }
// }



package com.project.practice.sap.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.practice.sap.dto.CreateVersionRequestDTO;
import com.project.practice.sap.dto.ReviewVersionRequestDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.exception.ForbiddenOperationException;
import com.project.practice.sap.exception.NotFoundException;
import com.project.practice.sap.exception.BadRequestException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.model.enums.VersionStatus;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;

@Service
@Transactional
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public VersionServiceImpl(
            VersionRepository versionRepository,
            DocumentRepository documentRepository,
            UserRepository userRepository) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VersionResponseDTO createVersion(Long documentId, CreateVersionRequestDTO request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        User creator = userRepository.findById(request.createdById())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!creator.hasRole(RoleType.AUTHOR) && !creator.hasRole(RoleType.ADMIN)) {
            throw new ForbiddenOperationException("User does not have permission to create versions");
        }

        int nextVersionNumber = versionRepository.findFirstByDocumentIdOrderByVersionNumberDesc(documentId)
                .map(version -> version.getVersionNumber() + 1)
                .orElse(1);

        Version version = new Version();
        version.setDocument(document);
        version.setVersionNumber(nextVersionNumber);
        version.setContent(request.content());
        version.setChangeSummary(request.changeSummary());
        version.setStatus(VersionStatus.REVIEW);
        version.setCreatedBy(creator);

        Version saved = versionRepository.save(version);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersionResponseDTO> getVersionsByDocumentId(Long documentId) {
        return versionRepository.findByDocumentIdOrderByVersionNumberAsc(documentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VersionResponseDTO getVersionById(Long versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));
        return toResponse(version);
    }

    @Override
    public VersionResponseDTO approveVersion(Long versionId, ReviewVersionRequestDTO request) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        if (version.getStatus() != VersionStatus.REVIEW) {
            throw new BadRequestException("Only REVIEW versions can be approved");
        }

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        if (!reviewer.hasRole(RoleType.REVIEWER) && !reviewer.hasRole(RoleType.ADMIN)) {
            throw new ForbiddenOperationException("User does not have permission to approve versions");
        }

        version.setStatus(VersionStatus.APPROVED);
        version.setReviewedBy(reviewer);
        version.setReviewedAt(LocalDateTime.now());
        version.setReviewComment(request.comment());

        return toResponse(versionRepository.save(version));
    }

    @Override
    public VersionResponseDTO rejectVersion(Long versionId, ReviewVersionRequestDTO request) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        if (version.getStatus() != VersionStatus.REVIEW) {
            throw new BadRequestException("Only REVIEW versions can be rejected");
        }

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        if (!reviewer.hasRole(RoleType.REVIEWER) && !reviewer.hasRole(RoleType.ADMIN)) {
            throw new ForbiddenOperationException("User does not have permission to reject versions");
        }

        version.setStatus(VersionStatus.REJECTED);
        version.setReviewedBy(reviewer);
        version.setReviewedAt(LocalDateTime.now());
        version.setReviewComment(request.comment());

        return toResponse(versionRepository.save(version));
    }

    @Override
    public VersionResponseDTO activateVersion(Long versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        if (version.getStatus() != VersionStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED versions can be activated");
        }

        Document document = version.getDocument();
        document.setActiveVersion(version);
        documentRepository.save(document);

        return toResponse(version);
    }

    private VersionResponseDTO toResponse(Version version) {
        String reviewedByUsername = version.getReviewedBy() != null
                ? version.getReviewedBy().getUsername()
                : null;

        return new VersionResponseDTO(
                version.getId(),
                version.getDocument().getId(),
                version.getVersionNumber(),
                version.getContent(),
                version.getChangeSummary(),
                version.getStatus(),
                version.getCreatedBy().getUsername(),
                version.getCreatedAt(),
                reviewedByUsername,
                version.getReviewedAt(),
                version.getReviewComment()
        );
    }
}