package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.dto.DocumentViewDTO;
import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.service.DocumentService;
import com.project.practice.sap.service.VersionService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DocumentPageController {

    private final DocumentService documentService;
    private final VersionService versionService;

    public DocumentPageController(DocumentService documentService,
                                  VersionService versionService) {
        this.documentService = documentService;
        this.versionService = versionService;
    }

    @GetMapping("/documents/create")
    public String createDocumentPage() {
        return "create-document";
    }

    @PostMapping("/documents/create")
    public String createDocument(@RequestParam String name,
                                 @RequestParam MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            DocumentResponseDTO created = documentService.createDocument(name, file);
            redirectAttributes.addFlashAttribute("successMessage", "Document created successfully.");
            return "redirect:/documents/" + created.id() + "/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/documents/create";
        }
    }

    @GetMapping("/documents/{id}/view")
    public String viewDocument(@PathVariable Integer id,
                               Model model,
                               Authentication authentication) {
        boolean isLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isLoggedIn);

        if (!isLoggedIn) {
            model.addAttribute("errorMessage", "You need to be logged in to view documents.");
            return "document-view";
        }

        try {
            DocumentViewDTO documentView = versionService.getActiveDocumentView(id);

            model.addAttribute("documentName", documentView.documentName());
            model.addAttribute("versionNum", documentView.versionNumber());
            model.addAttribute("content", documentView.content());

        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "document-view";
    }

    @GetMapping("/documents/{id}/delete")
    public String deleteDocumentPage(@PathVariable Integer id, Model model) {
        DocumentResponseDTO document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        return "delete-document";
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable Integer id,
                                 RedirectAttributes redirectAttributes) {
        documentService.deleteDocument(id);
        redirectAttributes.addFlashAttribute("successMessage", "Document deleted successfully.");
        return "redirect:/";
    }
}