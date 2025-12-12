package com.cms.projects.transformation.transformer;

import com.cms.projects.transformation.framemaker.model.FrameMakerChapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterMapper {
    
    public List<String> mapChapterTopics(FrameMakerChapter chapter, List<String> allTopicPaths, int startIndex) {
        List<String> chapterTopicPaths = new ArrayList<>();
        int topicCount = chapter.getSections().size();
        
        for (int i = 0; i < topicCount && (startIndex + i) < allTopicPaths.size(); i++) {
            chapterTopicPaths.add(allTopicPaths.get(startIndex + i));
        }
        
        return chapterTopicPaths;
    }
}

