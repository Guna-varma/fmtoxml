package com.cms.projects.transformation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "transformation_jobs")
public class TransformationJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Job ID cannot be blank")
    @Size(max = 255, message = "Job ID must not exceed 255 characters")
    @Column(nullable = false, unique = true, length = 255)
    private String jobId;
    
    @NotBlank(message = "Input file name cannot be blank")
    @Size(max = 500, message = "Input file name must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String inputFileName;
    
    @NotBlank(message = "Input file path cannot be blank")
    @Size(max = 1000, message = "Input file path must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String inputFilePath;
    
    @Size(max = 1000, message = "Output path must not exceed 1000 characters")
    @Column(length = 1000)
    private String outputPath;
    
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransformationStatus status;
    
    @Size(max = 2000, message = "Error message must not exceed 2000 characters")
    @Column(length = 2000)
    private String errorMessage;
    
    @NotNull(message = "Created date cannot be null")
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getInputFileName() {
        return inputFileName;
    }
    
    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }
    
    public String getInputFilePath() {
        return inputFilePath;
    }
    
    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public TransformationStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransformationStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public enum TransformationStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}

