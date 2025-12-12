package com.cms.projects.transformation.dita.generator;

import com.cms.projects.transformation.dita.model.DitaMap;
import com.cms.projects.transformation.dita.model.DitaTopic;
import com.cms.projects.transformation.framemaker.model.*;

import java.util.ArrayList;
import java.util.List;

public class DitaStructureBuilder {
    
    private final DitaTopicGenerator topicGenerator;
    private final DitaMapGenerator mapGenerator;
    
    public DitaStructureBuilder() {
        this.topicGenerator = new DitaTopicGenerator();
        this.mapGenerator = new DitaMapGenerator();
    }

    public List<DitaTopic> buildTopics(FrameMakerDocument document) {
        List<DitaTopic> topics = new ArrayList<>();
        int topicIndex = 1;
        
        for (FrameMakerChapter chapter : document.getChapters()) {
            for (FrameMakerSection section : chapter.getSections()) {
                String topicId = "topic_" + String.format("%03d", topicIndex);
                String fileName = topicId + ".xml";
                
                DitaTopic topic = topicGenerator.generateTopic(section, topicId, fileName);
                topics.add(topic);
                topicIndex++;
            }
        }
        
        return topics;
    }

    public DitaMap buildMainMap(FrameMakerDocument document, List<String> chapterMapPaths) {
        return mapGenerator.generateMainMap(document, chapterMapPaths);
    }

    public List<DitaMap> buildChapterMaps(FrameMakerDocument document, List<List<String>> topicPathsPerChapter) {
        List<DitaMap> chapterMaps = new ArrayList<>();
        int chapterIndex = 1;
        
        for (int i = 0; i < document.getChapters().size(); i++) {
            FrameMakerChapter chapter = document.getChapters().get(i);
            List<String> topicPaths = topicPathsPerChapter.size() > i ? topicPathsPerChapter.get(i) : new ArrayList<>();
            
            DitaMap chapterMap = mapGenerator.generateChapterMap(chapter, topicPaths, chapterIndex);
            chapterMaps.add(chapterMap);
            chapterIndex++;
        }
        
        return chapterMaps;
    }
}

