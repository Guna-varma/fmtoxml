package com.cms.projects.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for creating ZIP files from folders
 */
@Service
public class FolderZipService {
    
    private static final Logger logger = LoggerFactory.getLogger(FolderZipService.class);
    
    /**
     * Creates a ZIP file from a validated folder
     * 
     * @param folderPath Path to the folder to zip
     * @param outputZipPath Path where the ZIP file should be created
     * @return Path to the created ZIP file
     * @throws IOException if zipping fails
     * @throws IllegalArgumentException if folder does not exist or is not a directory
     */
    public Path zipFolder(Path folderPath, Path outputZipPath) throws IOException {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            throw new IllegalArgumentException("Folder does not exist or is not a directory: " + folderPath);
        }
        
        // Ensure parent directory exists
        if (outputZipPath.getParent() != null) {
            Files.createDirectories(outputZipPath.getParent());
        }
        
        long startTime = System.currentTimeMillis();
        final int[] fileCount = {0};
        
        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(outputZipPath.toFile()))) {
            
            Files.walk(folderPath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        String relativePath = folderPath.relativize(path)
                            .toString()
                            .replace('\\', '/');
                        
                        ZipEntry zipEntry = new ZipEntry(relativePath);
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                        
                        fileCount[0]++;
                        logger.debug("Added to ZIP: {}", relativePath);
                    } catch (IOException e) {
                        throw new RuntimeException("Error adding file to ZIP: " + path, e);
                    }
                });
        }
        
        long duration = System.currentTimeMillis() - startTime;
        long zipSize = Files.size(outputZipPath);
        
        logger.info("Successfully created ZIP file: {} (size: {} bytes, files: {}, duration: {}ms)", 
                   outputZipPath, zipSize, fileCount[0], duration);
        
        return outputZipPath;
    }
}

