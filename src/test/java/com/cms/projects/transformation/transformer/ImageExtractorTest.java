package com.cms.projects.transformation.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageExtractorTest {
    
    private ImageExtractor extractor;
    
    @BeforeEach
    void setUp() {
        extractor = new ImageExtractor();
    }
    
    @Test
    void testExtractImages(@TempDir Path tempDir) throws IOException {
        // Create a dummy image file
        File imageFile = tempDir.resolve("test.eps").toFile();
        try (FileWriter writer = new FileWriter(imageFile)) {
            writer.write("dummy EPS content");
        }
        
        File sourceFile = tempDir.resolve("source.mif").toFile();
        Path outputDir = tempDir.resolve("output/images");
        
        List<String> imageRefs = new ArrayList<>();
        imageRefs.add("test.eps");
        
        List<String> extracted = extractor.extractImages(imageRefs, sourceFile, outputDir);
        
        assertNotNull(extracted);
    }
}

