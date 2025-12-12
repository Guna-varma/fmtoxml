package com.cms.projects.transformation.dita.writer;

import com.cms.projects.transformation.dita.model.DitaMap;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DitaMapWriter {
    
    private static final String DITA_MAP_NAMESPACE = "http://docs.oasis-open.org/dita/v1.3/ns/200603/map.dtd";

    public void write(DitaMap ditaMap, Path outputPath) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element mapElement = document.addElement("map", DITA_MAP_NAMESPACE);
        mapElement.addAttribute("id", ditaMap.getId());
        
        // Title
        Element titleElement = mapElement.addElement("title");
        titleElement.addText(ditaMap.getTitle());
        
        // Topicrefs
        for (DitaMap.DitaMapReference reference : ditaMap.getReferences()) {
            addTopicref(mapElement, reference);
        }
        
        // Write to file
        Files.createDirectories(outputPath.getParent());
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setIndentSize(2);
        
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            XMLWriter xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(document);
            xmlWriter.close();
        }
    }

    private void addTopicref(Element parent, DitaMap.DitaMapReference reference) {
        Element topicref = parent.addElement("topicref");
        
        if (reference.getHref() != null) {
            topicref.addAttribute("href", reference.getHref());
        }
        
        if (reference.getFormat() != null) {
            topicref.addAttribute("format", reference.getFormat());
        }
        
        if (reference.getType() != null) {
            topicref.addAttribute("type", reference.getType());
        }
        
        if (reference.getTitle() != null) {
            topicref.addAttribute("navtitle", reference.getTitle());
        }
        
        // Add children
        for (DitaMap.DitaMapReference child : reference.getChildren()) {
            addTopicref(topicref, child);
        }
    }
}

