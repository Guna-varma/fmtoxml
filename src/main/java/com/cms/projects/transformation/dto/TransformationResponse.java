package com.cms.projects.transformation.dto;

import com.cms.projects.transformation.entity.TransformationJob;
import java.time.LocalDateTime;

public class TransformationResponse {
    private String jobId;
    private TransformationJob.TransformationStatus status;
    private String message;
    private LocalDateTime createdAt;
    private String outputPath;

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
}

