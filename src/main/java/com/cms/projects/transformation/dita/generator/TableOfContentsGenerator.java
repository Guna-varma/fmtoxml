package com.cms.projects.transformation.dita.generator;

import com.cms.projects.transformation.dita.model.TocEntry;
import com.cms.projects.transformation.framemaker.model.*;

import java.util.ArrayList;
import java.util.List;

public class TableOfContentsGenerator {
    
    public List<TocEntry> generateToc(FrameMakerDocument document) {
        List<TocEntry> tocEntries = new ArrayList<>();
        
        int topicIndex = 1;
        int chapterIndex = 1;
        
        for (FrameMakerChapter chapter : document.getChapters()) {
            // Create chapter entry
            TocEntry chapterEntry = new TocEntry();
            chapterEntry.setTitle(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapterIndex);
            chapterEntry.setType("chapter");
            chapterEntry.setHref("chapters/chapter_" + String.format("%02d", chapterIndex) + ".ditamap");
            chapterEntry.setLevel(1);
            
            // Add sections/topics as children
            for (FrameMakerSection section : chapter.getSections()) {
                TocEntry topicEntry = new TocEntry();
                topicEntry.setTitle(section.getTitle() != null ? section.getTitle() : "Untitled Section");
                topicEntry.setType("topic");
                topicEntry.setHref("../xml/topic_" + String.format("%03d", topicIndex) + ".xml");
                topicEntry.setLevel(2);
                
                // Add subsections if any
                if (section.getLevel() > 1) {
                    topicEntry.setLevel(section.getLevel() + 1);
                }
                
                chapterEntry.addChild(topicEntry);
                topicIndex++;
            }
            
            tocEntries.add(chapterEntry);
            chapterIndex++;
        }
        
        // Add table references if any
        if (!document.getTableReferences().isEmpty()) {
            for (String tableRef : document.getTableReferences()) {
                TocEntry tableEntry = new TocEntry();
                tableEntry.setTitle("Table: " + tableRef);
                tableEntry.setType("table");
                tableEntry.setLevel(2);
                tocEntries.add(tableEntry);
            }
        }
        
        return tocEntries;
    }
}

