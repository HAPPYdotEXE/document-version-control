package com.project.practice.sap.service;

import com.project.practice.sap.exception.InvalidFileException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileStorageService {

    public void validateTxtFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".txt")) {
            throw new InvalidFileException("Only .txt files are accepted. Received: " + originalName);
        }
    }

    // saves the uploaded file to: uploads/documents/{documentId}/{versionNum}.txt
    // returns the path string that is stored in Version.filePath
    public String saveFileToDisk(MultipartFile file, Integer documentId, String versionNum) {
        try {
            Path directory = Files.createDirectories(
                    Path.of("uploads", "documents", String.valueOf(documentId)));
            Path filePath = directory.resolve(versionNum + ".txt");
            file.transferTo(filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file to disk: " + e.getMessage(), e);
        }
    }

    // loads a file from disk using the path stored in Version.filePath
    public Resource loadFileAsResource(String storedFilePath) {
        try {
            Path filePath = Path.of(storedFilePath).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or is damaged: " + filePath);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not resolve file path: " + e.getMessage(), e);
        }
    }
}
