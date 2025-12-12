package com.cms.projects.transformation.transformer;

import com.cms.projects.transformation.dita.model.DitaElement;
import com.cms.projects.transformation.framemaker.model.FrameMakerParagraph;

import java.util.ArrayList;
import java.util.List;

public class ContentMapper {
    
    public List<DitaElement> mapParagraphs(List<FrameMakerParagraph> paragraphs) {
        List<DitaElement> elements = new ArrayList<>();
        
        for (FrameMakerParagraph paragraph : paragraphs) {
            DitaElement element = mapParagraph(paragraph);
            if (element != null) {
                elements.add(element);
            }
        }
        
        return elements;
    }
    
    private DitaElement mapParagraph(FrameMakerParagraph paragraph) {
        DitaElement element = new DitaElement();
        
        if (paragraph.isTable()) {
            element.setType("simpletable");
        } else {
            element.setType("p");
        }
        
        if (paragraph.getText() != null) {
            element.setContent(paragraph.getText());
        }
        
        // Handle image references
        if (!paragraph.getImageReferences().isEmpty()) {
            for (String imageRef : paragraph.getImageReferences()) {
                DitaElement imageElement = new DitaElement();
                imageElement.setType("image");
                String imagePath = "../images/" + extractImageFileName(imageRef);
                imageElement.setHref(imagePath);
                element.addChild(imageElement);
            }
        }
        
        return element;
    }
    
    private String extractImageFileName(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "image.eps";
        }
        
        int lastSlash = Math.max(imagePath.lastIndexOf('/'), imagePath.lastIndexOf('\\'));
        String fileName = lastSlash >= 0 ? imagePath.substring(lastSlash + 1) : imagePath;
        
        if (!fileName.toLowerCase().endsWith(".eps") && 
            !fileName.toLowerCase().endsWith(".png") && 
            !fileName.toLowerCase().endsWith(".jpg")) {
            fileName += ".eps";
        }
        
        return fileName;
    }
}

