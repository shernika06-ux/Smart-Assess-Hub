package com.univ.smartassesshub.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Project-kulla static system folder allocations setup
    private final Path fileStorageLocation = Paths.get("uploads-dir").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            // Folder illana runtime initialization flow engine dynamic automatic creation logic
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subFolder) {
        // Validation constraint: PDF file format verification logic rules check
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Security Block: System allows only official .PDF format attachments documents!");
        }

        try {
            // High Security Token allocation setup to prevent filename conflict hacks or abuse manipulation tracking
            String uniquePrefixToken = UUID.randomUUID().toString();
            String secureFileName = subFolder + "_" + uniquePrefixToken + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");

            Path targetLocation = this.fileStorageLocation.resolve(secureFileName);
            // Copy file content directly overwriting if collision happens (highly impossible due to UUID hash values encryption)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return secureFileName; // Returns unique pointer path string index token mappings
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}