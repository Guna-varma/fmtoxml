package com.cms.projects.transformation.dita.generator;

import com.cms.projects.transformation.dita.model.DitaTopic;
import com.cms.projects.transformation.framemaker.model.FrameMakerParagraph;
import com.cms.projects.transformation.framemaker.model.FrameMakerSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DitaTopicGeneratorTest {
    
    private DitaTopicGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new DitaTopicGenerator();
    }
    
    @Test
    void testGenerateTopic() {
        FrameMakerSection section = new FrameMakerSection();
        section.setTitle("Test Section");
        
        FrameMakerParagraph para1 = new FrameMakerParagraph();
        para1.setText("This is a test paragraph.");
        section.addParagraph(para1);
        
        DitaTopic topic = generator.generateTopic(section, "topic_001", "topic_001.xml");
        
        assertNotNull(topic);
        assertEquals("topic_001", topic.getId());
        assertEquals("Test Section", topic.getTitle());
        assertEquals("topic_001.xml", topic.getFileName());
        assertFalse(topic.getContent().isEmpty());
    }
    
    @Test
    void testGenerateTopicWithImage() {
        FrameMakerSection section = new FrameMakerSection();
        section.setTitle("Section with Image");
        
        FrameMakerParagraph para = new FrameMakerParagraph();
        para.setText("Paragraph with image");
        para.addImageReference("image.eps");
        section.addParagraph(para);
        
        DitaTopic topic = generator.generateTopic(section, "topic_002", "topic_002.xml");
        
        assertNotNull(topic);
        assertFalse(topic.getContent().isEmpty());
    }
}

