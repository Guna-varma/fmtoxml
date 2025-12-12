package com.cms.projects.transformation.service;

import com.cms.projects.transformation.dto.FolderTransformationResponse;
import com.cms.projects.transformation.dto.TransformationResponse;
import com.cms.projects.transformation.entity.TransformationJob;

public interface TransformationService {
    TransformationResponse submitTransformation(String filePath, String fileName);
    FolderTransformationResponse submitFolderTransformation(String zipFilePath, String zipFileName);
    TransformationJob.TransformationStatus getJobStatus(String jobId);
    TransformationResponse getJobResult(String jobId);
}

