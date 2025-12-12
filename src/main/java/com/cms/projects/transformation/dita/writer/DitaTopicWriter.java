package com.cms.projects.transformation.dita.writer;

import com.cms.projects.transformation.dita.model.DitaElement;
import com.cms.projects.transformation.dita.model.DitaTopic;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DitaTopicWriter {
    
    private static final String DITA_NAMESPACE = "http://docs.oasis-open.org/dita/v1.3/ns/200603/topic.dtd";

    public void write(DitaTopic topic, Path outputPath) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element topicElement = document.addElement("topic", DITA_NAMESPACE);
        topicElement.addAttribute("id", topic.getId());
        
        // Title
        Element titleElement = topicElement.addElement("title");
        titleElement.addText(topic.getTitle());
        
        // Body
        Element bodyElement = topicElement.addElement("body");
        
        // Add content elements
        for (DitaElement element : topic.getContent()) {
            createElement(bodyElement, element);
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

    private Element createElement(Element parent, DitaElement ditaElement) {
        Element element = parent.addElement(ditaElement.getType());
        
        if (ditaElement.getId() != null) {
            element.addAttribute("id", ditaElement.getId());
        }
        
        if (ditaElement.getHref() != null) {
            element.addAttribute("href", ditaElement.getHref());
        }
        
        if (ditaElement.getContent() != null && !ditaElement.getContent().isEmpty()) {
            element.addText(ditaElement.getContent());
        }
        
        for (DitaElement child : ditaElement.getChildren()) {
            createElement(element, child);
        }
        
        return element;
    }
}

