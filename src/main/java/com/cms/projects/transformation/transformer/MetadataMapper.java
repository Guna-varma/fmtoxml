package com.cms.projects.transformation.transformer;

import com.cms.projects.transformation.framemaker.model.FrameMakerDocument;

public class MetadataMapper {
    
    public String mapDocumentTitle(FrameMakerDocument document) {
        if (document.getTitle() != null && !document.getTitle().isEmpty()) {
            return document.getTitle();
        }
        return "Transformed Document";
    }
    
    public String mapDocumentId(FrameMakerDocument document) {
        String fileName = document.getFilePath();
        if (fileName != null && !fileName.isEmpty()) {
            int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
            String name = lastSlash >= 0 ? fileName.substring(lastSlash + 1) : fileName;
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                return name.substring(0, dotIndex).replaceAll("[^a-zA-Z0-9]", "_");
            }
            return name.replaceAll("[^a-zA-Z0-9]", "_");
        }
        return "document_1";
    }
}

