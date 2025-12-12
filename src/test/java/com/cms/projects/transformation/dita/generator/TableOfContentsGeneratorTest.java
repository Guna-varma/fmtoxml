package com.cms.projects.transformation.dita.generator;

import com.cms.projects.transformation.dita.model.TocEntry;
import com.cms.projects.transformation.framemaker.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableOfContentsGeneratorTest {
    
    private TableOfContentsGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new TableOfContentsGenerator();
    }
    
    @Test
    void testGenerateToc() {
        FrameMakerDocument document = new FrameMakerDocument();
        document.setTitle("Test Document");
        
        FrameMakerChapter chapter1 = new FrameMakerChapter();
        chapter1.setTitle("Chapter 1");
        chapter1.setChapterNumber("1");
        
        FrameMakerSection section1 = new FrameMakerSection();
        section1.setTitle("Section 1.1");
        chapter1.addSection(section1);
        
        document.addChapter(chapter1);
        
        List<TocEntry> tocEntries = generator.generateToc(document);
        
        assertNotNull(tocEntries);
        assertFalse(tocEntries.isEmpty());
        assertEquals("Chapter 1", tocEntries.get(0).getTitle());
        assertFalse(tocEntries.get(0).getChildren().isEmpty());
    }
}

