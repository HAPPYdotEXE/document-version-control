package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.service.DocumentService;
import com.project.practice.sap.service.VersionService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VersionPageController {

    private final VersionService versionService;
    private final DocumentService documentService;

    public VersionPageController(VersionService versionService,
                                 DocumentService documentService) {
        this.versionService = versionService;
        this.documentService = documentService;
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    @GetMapping("/documents/{documentId}/versions")
    public String versionsPage(@PathVariable Integer documentId,
                               Authentication authentication,
                               Model model) {

        boolean isLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        boolean canReview = false;
        boolean canUploadVersion = false;

        if (isLoggedIn) {
            canReview = authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            "ROLE_REVIEWER".equals(a.getAuthority()) ||
                                    "ROLE_ADMIN".equals(a.getAuthority()));

            canUploadVersion = authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            "ROLE_AUTHOR".equals(a.getAuthority()) ||
                                    "ROLE_ADMIN".equals(a.getAuthority()));
        }

        DocumentResponseDTO document = documentService.getDocumentById(documentId);
        List<VersionResponseDTO> versions = versionService.getVersionHistory(documentId);

        model.addAttribute("document", document);
        model.addAttribute("versions", versions);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("canReview", canReview);
        model.addAttribute("canUploadVersion", canUploadVersion);

        return "versions";
    }

    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    @PostMapping("/documents/{documentId}/versions/{versionNum}/approve")
    public String approveVersion(@PathVariable Integer documentId,
                                 @PathVariable Integer versionNum,
                                 @RequestParam(required = false) String comment,
                                 RedirectAttributes redirectAttributes) {
        try {
            versionService.approveVersion(documentId, versionNum, comment);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Version v" + versionNum + " was approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/documents/" + documentId + "/versions";
    }

    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    @PostMapping("/documents/{documentId}/versions/{versionNum}/reject")
    public String rejectVersion(@PathVariable Integer documentId,
                                @PathVariable Integer versionNum,
                                @RequestParam(required = false) String comment,
                                RedirectAttributes redirectAttributes) {
        try {
            versionService.rejectVersion(documentId, versionNum, comment);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Version v" + versionNum + " was rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/documents/" + documentId + "/versions";
    }

    @PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
    @GetMapping("/documents/{documentId}/versions/{versionNum}/download")
    public ResponseEntity<Resource> downloadVersion(@PathVariable Integer documentId,
                                                    @PathVariable Integer versionNum) {
        Resource file = versionService.downloadFile(documentId, versionNum);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}