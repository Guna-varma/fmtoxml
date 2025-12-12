package com.cms.projects.transformation.dita.generator;

import com.cms.projects.transformation.dita.model.DitaMap;
import com.cms.projects.transformation.framemaker.model.FrameMakerChapter;
import com.cms.projects.transformation.framemaker.model.FrameMakerDocument;

import java.util.List;

public class DitaMapGenerator {
    
    public DitaMap generateMainMap(FrameMakerDocument document, List<String> chapterMapPaths) {
        DitaMap mainMap = new DitaMap();
        mainMap.setId("main-map");
        mainMap.setTitle(document.getTitle() != null ? document.getTitle() : "Main Document");
        mainMap.setFileName("main.ditamap");
        
        // Add references to chapter maps
        for (int i = 0; i < chapterMapPaths.size(); i++) {
            DitaMap.DitaMapReference chapterRef = new DitaMap.DitaMapReference();
            chapterRef.setHref("chapters/chapter_" + String.format("%02d", i + 1) + ".ditamap");
            chapterRef.setFormat("ditamap");
            chapterRef.setType("map");
            
            if (i < document.getChapters().size()) {
                chapterRef.setTitle(document.getChapters().get(i).getTitle());
            }
            
            mainMap.addReference(chapterRef);
        }
        
        return mainMap;
    }

    public DitaMap generateChapterMap(FrameMakerChapter chapter, List<String> topicPaths, int chapterIndex) {
        DitaMap chapterMap = new DitaMap();
        chapterMap.setId("chapter-" + chapterIndex);
        chapterMap.setTitle(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapterIndex);
        chapterMap.setFileName("chapter_" + String.format("%02d", chapterIndex) + ".ditamap");
        
        // Add references to topics
        int topicIndex = 0;
        for (String topicPath : topicPaths) {
            DitaMap.DitaMapReference topicRef = new DitaMap.DitaMapReference();
            topicRef.setHref("../xml/" + topicPath);
            topicRef.setFormat("dita");
            topicRef.setType("topic");
            
            if (topicIndex < chapter.getSections().size()) {
                topicRef.setTitle(chapter.getSections().get(topicIndex).getTitle());
            }
            
            chapterMap.addReference(topicRef);
            topicIndex++;
        }
        
        return chapterMap;
    }
}

