package com.cms.projects.transformation.dita.writer;

import com.cms.projects.transformation.dita.model.TocEntry;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TableOfContentsWriter {
    
    private static final String TOC_NAMESPACE = "http://docs.oasis-open.org/dita/v1.3/ns/200603/toc";

    public void write(List<TocEntry> entries, Path outputPath) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element tocElement = document.addElement("toc", TOC_NAMESPACE);
        tocElement.addAttribute("xmlns", TOC_NAMESPACE);
        
        // Add title
        Element titleElement = tocElement.addElement("title");
        titleElement.addText("Table of Contents");
        
        // Add entries
        for (TocEntry entry : entries) {
            addEntry(tocElement, entry);
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

    private void addEntry(Element parent, TocEntry entry) {
        Element entryElement = parent.addElement("entry");
        
        if (entry.getTitle() != null) {
            entryElement.addAttribute("title", entry.getTitle());
        }
        
        if (entry.getHref() != null) {
            entryElement.addAttribute("href", entry.getHref());
        }
        
        if (entry.getType() != null) {
            entryElement.addAttribute("type", entry.getType());
        }
        
        entryElement.addAttribute("level", String.valueOf(entry.getLevel()));
        
        // Add children
        for (TocEntry child : entry.getChildren()) {
            addEntry(entryElement, child);
        }
    }
}

