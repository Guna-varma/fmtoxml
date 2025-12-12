package com.cms.projects.transformation.transformer;

import com.cms.projects.transformation.dita.generator.*;
import com.cms.projects.transformation.dita.model.*;
import com.cms.projects.transformation.dita.writer.*;
import com.cms.projects.transformation.framemaker.exception.FrameMakerParseException;
import com.cms.projects.transformation.framemaker.exception.FrameMakerUnsupportedFormatException;
import com.cms.projects.transformation.framemaker.model.FrameMakerDocument;
import com.cms.projects.transformation.framemaker.parser.FrameMakerMifParser;
import com.cms.projects.transformation.framemaker.parser.FrameMakerParser;
import com.cms.projects.transformation.framemaker.parser.FrameMakerBinaryParser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FrameMakerToDitaTransformer {
    
    private final FrameMakerMifParser mifParser;
    private final FrameMakerBinaryParser binaryParser;
    private final DitaStructureBuilder structureBuilder;
    private final DitaTopicWriter topicWriter;
    private final DitaMapWriter mapWriter;
    private final TableOfContentsGenerator tocGenerator;
    private final TableOfContentsWriter tocWriter;
    private final ImageExtractor imageExtractor;
    private final TableExtractor tableExtractor;
    
    public FrameMakerToDitaTransformer() {
        this.mifParser = new FrameMakerMifParser();
        this.binaryParser = new FrameMakerBinaryParser();
        this.structureBuilder = new DitaStructureBuilder();
        this.topicWriter = new DitaTopicWriter();
        this.mapWriter = new DitaMapWriter();
        this.tocGenerator = new TableOfContentsGenerator();
        this.tocWriter = new TableOfContentsWriter();
        this.imageExtractor = new ImageExtractor();
        this.tableExtractor = new TableExtractor();
    }
    
    public void transform(File inputFile, Path outputDir) 
            throws FrameMakerParseException, FrameMakerUnsupportedFormatException, IOException {
        
        // Create output directory structure
        Path xmlDir = outputDir.resolve("xml");
        Path imagesDir = outputDir.resolve("images");
        Path chaptersDir = outputDir.resolve("chapters");
        Path tocDir = outputDir.resolve("table-of-contents");
        
        Files.createDirectories(xmlDir);
        Files.createDirectories(imagesDir);
        Files.createDirectories(chaptersDir);
        Files.createDirectories(tocDir);
        
        // Parse FrameMaker file
        FrameMakerDocument document = parseFrameMakerFile(inputFile);
        
        // Extract images
        List<String> imageReferences = document.getAllImageReferences();
        if (!imageReferences.isEmpty()) {
            imageExtractor.extractImages(imageReferences, inputFile, imagesDir);
        }
        
        // Build DITA topics
        List<DitaTopic> topics = structureBuilder.buildTopics(document);
        
        // Write topics to xml/
        List<String> allTopicPaths = new ArrayList<>();
        for (DitaTopic topic : topics) {
            Path topicPath = xmlDir.resolve(topic.getFileName());
            topicWriter.write(topic, topicPath);
            allTopicPaths.add(topic.getFileName());
        }
        
        // Build chapter maps
        List<List<String>> topicPathsPerChapter = new ArrayList<>();
        int topicIndex = 0;
        for (var chapter : document.getChapters()) {
            List<String> chapterTopics = new ArrayList<>();
            int sectionCount = chapter.getSections().size();
            for (int i = 0; i < sectionCount && topicIndex < allTopicPaths.size(); i++) {
                chapterTopics.add(allTopicPaths.get(topicIndex++));
            }
            topicPathsPerChapter.add(chapterTopics);
        }
        
        List<DitaMap> chapterMaps = structureBuilder.buildChapterMaps(document, topicPathsPerChapter);
        
        // Write chapter maps to chapters/
        List<String> chapterMapPaths = new ArrayList<>();
        for (DitaMap chapterMap : chapterMaps) {
            Path chapterMapPath = chaptersDir.resolve(chapterMap.getFileName());
            mapWriter.write(chapterMap, chapterMapPath);
            chapterMapPaths.add(chapterMap.getFileName());
        }
        
        // Generate and write main map
        DitaMap mainMap = structureBuilder.buildMainMap(document, chapterMapPaths);
        Path mainMapPath = outputDir.resolve(mainMap.getFileName());
        mapWriter.write(mainMap, mainMapPath);
        
        // Generate and write table of contents
        List<TocEntry> tocEntries = tocGenerator.generateToc(document);
        Path tocPath = tocDir.resolve("toc.xml");
        tocWriter.write(tocEntries, tocPath);
        
        // Validate output
        validateOutput(outputDir);
    }
    
    private FrameMakerDocument parseFrameMakerFile(File file) 
            throws FrameMakerParseException, FrameMakerUnsupportedFormatException {
        
        FrameMakerParser parser;
        if (binaryParser.supports(file)) {
            throw new FrameMakerUnsupportedFormatException(
                "Binary FrameMaker (.fm) files are not supported. Please use MIF format."
            );
        } else if (mifParser.supports(file)) {
            parser = mifParser;
        } else {
            throw new FrameMakerUnsupportedFormatException(
                "Unsupported file format: " + file.getName()
            );
        }
        
        return parser.parse(file);
    }
    
    private void validateOutput(Path outputDir) throws IOException {
        // Check that main.ditamap exists
        Path mainMap = outputDir.resolve("main.ditamap");
        if (!Files.exists(mainMap)) {
            throw new IOException("Main DITA map not generated");
        }
        
        // Check that toc.xml exists
        Path tocFile = outputDir.resolve("table-of-contents").resolve("toc.xml");
        if (!Files.exists(tocFile)) {
            throw new IOException("Table of contents not generated");
        }
        
        // Validate XML files are well-formed (basic check)
        validateXmlFiles(outputDir);
    }
    
    private void validateXmlFiles(Path outputDir) throws IOException {
        // Basic validation - check that files exist and are not empty
        Path xmlDir = outputDir.resolve("xml");
        if (Files.exists(xmlDir)) {
            Files.list(xmlDir)
                .filter(path -> path.toString().endsWith(".xml"))
                .forEach(path -> {
                    try {
                        if (Files.size(path) == 0) {
                            throw new RuntimeException("Empty XML file: " + path);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Error validating file: " + path, e);
                    }
                });
        }
    }
}

