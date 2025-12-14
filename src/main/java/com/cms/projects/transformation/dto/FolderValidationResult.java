package com.cms.projects.transformation.dto;

import java.nio.file.Path;
import java.util.List;

/**
 * DTO representing the result of folder validation
 */
public class FolderValidationResult {
    
    public enum FileType {
        FM, MIF
    }
    
    private boolean valid;
    private FileType fileType;
    private String message;
    private List<Path> files;
    private int fileCount;
    private List<String> fileNames;
    
    private FolderValidationResult(boolean valid, FileType fileType, String message, 
                                  List<Path> files, int fileCount) {
        this.valid = valid;
        this.fileType = fileType;
        this.message = message;
        this.files = files;
        this.fileCount = fileCount;
        this.fileNames = files != null ? 
            files.stream()
                .map(path -> path.getFileName().toString())
                .toList() : 
            List.of();
    }
    
    public static FolderValidationResult valid(FileType fileType, List<Path> files, int count) {
        String message = String.format(
            "Validation successful: Folder contains %d %s file(s)",
            count, 
            fileType == FileType.FM ? ".fm" : ".mif"
        );
        return new FolderValidationResult(true, fileType, message, files, count);
    }
    
    public static FolderValidationResult invalid(String message) {
        return new FolderValidationResult(false, null, message, List.of(), 0);
    }
    
    // Getters
    public boolean isValid() { 
        return valid; 
    }
    
    public FileType getFileType() { 
        return fileType; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public List<Path> getFiles() { 
        return files; 
    }
    
    public int getFileCount() { 
        return fileCount; 
    }
    
    public List<String> getFileNames() { 
        return fileNames; 
    }
}

