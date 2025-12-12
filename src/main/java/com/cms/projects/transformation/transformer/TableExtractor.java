package com.cms.projects.transformation.transformer;

import com.cms.projects.transformation.framemaker.model.FrameMakerDocument;

import java.util.ArrayList;
import java.util.List;

public class TableExtractor {
    
    public List<String> extractTableReferences(FrameMakerDocument document) {
        List<String> tableRefs = new ArrayList<>();
        
        for (var chapter : document.getChapters()) {
            for (var section : chapter.getSections()) {
                for (var paragraph : section.getParagraphs()) {
                    if (paragraph.isTable()) {
                        String tableId = "table_" + (tableRefs.size() + 1);
                        tableRefs.add(tableId);
                    }
                }
            }
        }
        
        return tableRefs;
    }
}

