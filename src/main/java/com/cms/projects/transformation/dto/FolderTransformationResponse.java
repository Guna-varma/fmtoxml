package com.cms.projects.transformation.dto;

import com.cms.projects.transformation.entity.TransformationJob;
import java.time.LocalDateTime;
import java.util.List;

public class FolderTransformationResponse {
    private String jobId;
    private TransformationJob.TransformationStatus status;
    private String message;
    private LocalDateTime createdAt;
    private String outputPath;
    private Integer totalFiles;
    private Integer processedFiles;
    private Integer failedFiles;
    private List<String> processedFileNames;
    private List<String> failedFileNames;
    
    // Validation information
    private Boolean validationPassed;
    private String validationMessage;
    private String fileType; // FM or MIF
    private Integer validatedFileCount;
    private List<String> validatedFileNames;

    public FolderTransformationResponse() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public TransformationJob.TransformationStatus getStatus() {
        return status;
    }

    public void setStatus(TransformationJob.TransformationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Integer getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(Integer totalFiles) {
        this.totalFiles = totalFiles;
    }

    public Integer getProcessedFiles() {
        return processedFiles;
    }

    public void setProcessedFiles(Integer processedFiles) {
        this.processedFiles = processedFiles;
    }

    public Integer getFailedFiles() {
        return failedFiles;
    }

    public void setFailedFiles(Integer failedFiles) {
        this.failedFiles = failedFiles;
    }

    public List<String> getProcessedFileNames() {
        return processedFileNames;
    }

    public void setProcessedFileNames(List<String> processedFileNames) {
        this.processedFileNames = processedFileNames;
    }

    public List<String> getFailedFileNames() {
        return failedFileNames;
    }

    public void setFailedFileNames(List<String> failedFileNames) {
        this.failedFileNames = failedFileNames;
    }
    
    // Validation getters and setters
    public Boolean getValidationPassed() {
        return validationPassed;
    }
    
    public void setValidationPassed(Boolean validationPassed) {
        this.validationPassed = validationPassed;
    }
    
    public String getValidationMessage() {
        return validationMessage;
    }
    
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Integer getValidatedFileCount() {
        return validatedFileCount;
    }
    
    public void setValidatedFileCount(Integer validatedFileCount) {
        this.validatedFileCount = validatedFileCount;
    }
    
    public List<String> getValidatedFileNames() {
        return validatedFileNames;
    }
    
    public void setValidatedFileNames(List<String> validatedFileNames) {
        this.validatedFileNames = validatedFileNames;
    }
}

