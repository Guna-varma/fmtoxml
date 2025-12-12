package com.cms.projects.transformation.dita.generator;

import com.cms.projects.transformation.dita.model.DitaElement;
import com.cms.projects.transformation.dita.model.DitaTopic;
import com.cms.projects.transformation.framemaker.model.FrameMakerParagraph;
import com.cms.projects.transformation.framemaker.model.FrameMakerSection;

import java.util.ArrayList;
import java.util.List;

public class DitaTopicGenerator {
    
    public DitaTopic generateTopic(FrameMakerSection section, String topicId, String fileName) {
        DitaTopic topic = new DitaTopic();
        topic.setId(topicId);
        topic.setTitle(section.getTitle() != null ? section.getTitle() : "Untitled Section");
        topic.setFileName(fileName);
        
        // Convert paragraphs to DITA elements
        List<DitaElement> content = new ArrayList<>();
        
        for (FrameMakerParagraph paragraph : section.getParagraphs()) {
            if (paragraph.isTable()) {
                // Handle table - create a simpletable element
                DitaElement tableElement = createTableElement(paragraph);
                content.add(tableElement);
            } else {
                // Regular paragraph
                DitaElement paraElement = createParagraphElement(paragraph);
                content.add(paraElement);
            }
        }
        
        topic.setContent(content);
        return topic;
    }

    private DitaElement createParagraphElement(FrameMakerParagraph paragraph) {
        DitaElement paraElement = new DitaElement();
        paraElement.setType("p");
        
        if (paragraph.getText() != null && !paragraph.getText().isEmpty()) {
            paraElement.setContent(paragraph.getText());
        }
        
        // Handle image references
        if (!paragraph.getImageReferences().isEmpty()) {
            DitaElement imageElement = new DitaElement();
            imageElement.setType("image");
            
            // Use first image reference
            String imageRef = paragraph.getImageReferences().get(0);
            String imagePath = "../images/" + extractImageFileName(imageRef);
            imageElement.setHref(imagePath);
            
            paraElement.addChild(imageElement);
        }
        
        return paraElement;
    }

    private DitaElement createTableElement(FrameMakerParagraph paragraph) {
        DitaElement tableElement = new DitaElement();
        tableElement.setType("simpletable");
        
        // Create a simple table structure
        if (paragraph.getText() != null && !paragraph.getText().isEmpty()) {
            DitaElement titleElement = new DitaElement();
            titleElement.setType("sthead");
            DitaElement titlePara = new DitaElement();
            titlePara.setType("stentry");
            titlePara.setContent(paragraph.getText());
            titleElement.addChild(titlePara);
            tableElement.addChild(titleElement);
        }
        
        return tableElement;
    }

    private String extractImageFileName(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "image.eps";
        }
        
        // Extract filename from path
        int lastSlash = Math.max(imagePath.lastIndexOf('/'), imagePath.lastIndexOf('\\'));
        String fileName = lastSlash >= 0 ? imagePath.substring(lastSlash + 1) : imagePath;
        
        // Ensure it has .eps extension if it's an image reference
        if (!fileName.toLowerCase().endsWith(".eps") && 
            !fileName.toLowerCase().endsWith(".png") && 
            !fileName.toLowerCase().endsWith(".jpg")) {
            fileName += ".eps";
        }
        
        return fileName;
    }
}

