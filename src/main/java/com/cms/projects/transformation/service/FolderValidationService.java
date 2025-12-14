package com.cms.projects.transformation.service;

import com.cms.projects.transformation.dto.FolderValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for validating folder structure according to business rules:
 * 1. Folder must contain only .fm files
 * 2. Folder must contain only .mif files
 * 3. Mixed .fm + .mif is invalid
 */
@Service
public class FolderValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FolderValidationService.class);
    
    private static final String FM_EXTENSION = ".fm";
    private static final String MIF_EXTENSION = ".mif";
    
    /**
     * Validates folder structure according to business rules:
     * 1. Folder must contain only .fm files
     * 2. Folder must contain only .mif files
     * 3. Mixed .fm + .mif is invalid
     * 
     * @param folderPath Path to the folder to validate
     * @return FolderValidationResult containing validation status and details
     * @throws IOException if folder access fails
     */
    public FolderValidationResult validateFolder(Path folderPath) throws IOException {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return FolderValidationResult.invalid("Folder does not exist or is not a directory");
        }
        
        List<Path> fmFiles = findFilesByExtension(folderPath, FM_EXTENSION);
        List<Path> mifFiles = findFilesByExtension(folderPath, MIF_EXTENSION);
        
        int fmCount = fmFiles.size();
        int mifCount = mifFiles.size();
        
        logger.debug("Found {} .fm files and {} .mif files in folder: {}", 
                    fmCount, mifCount, folderPath);
        
        // Rule 1: Folder must contain only .fm files
        if (fmCount > 0 && mifCount == 0) {
            logger.info("Validation passed: Folder contains only .fm files (count: {})", fmCount);
            return FolderValidationResult.valid(
                FolderValidationResult.FileType.FM, 
                fmFiles, 
                fmCount
            );
        }
        
        // Rule 2: Folder must contain only .mif files
        if (mifCount > 0 && fmCount == 0) {
            logger.info("Validation passed: Folder contains only .mif files (count: {})", mifCount);
            return FolderValidationResult.valid(
                FolderValidationResult.FileType.MIF, 
                mifFiles, 
                mifCount
            );
        }
        
        // Rule 3: Mixed .fm + .mif is invalid
        if (fmCount > 0 && mifCount > 0) {
            String errorMessage = String.format(
                "Invalid folder structure: Mixed file types detected. Found %d .fm files and %d .mif files. " +
                "Folder must contain only .fm files OR only .mif files, not both.",
                fmCount, mifCount
            );
            logger.warn("Validation failed: {}", errorMessage);
            return FolderValidationResult.invalid(errorMessage);
        }
        
        // No FrameMaker files found
        String errorMessage = "No FrameMaker files (.fm or .mif) found in the folder";
        logger.warn("Validation failed: {}", errorMessage);
        return FolderValidationResult.invalid(errorMessage);
    }
    
    /**
     * Recursively finds all files with the given extension
     * 
     * @param directory Directory to search
     * @param extension File extension to search for (e.g., ".fm", ".mif")
     * @return List of paths to files with the specified extension
     * @throws IOException if directory access fails
     */
    private List<Path> findFilesByExtension(Path directory, String extension) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return fileName.endsWith(extension.toLowerCase());
                })
                .toList();
        }
    }
}

