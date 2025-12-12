package com.cms.projects.transformation.dita.model;

import java.util.ArrayList;
import java.util.List;

public class DitaTopic {
    private String id;
    private String title;
    private List<DitaElement> content;
    private String fileName;

    public DitaTopic() {
        this.content = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DitaElement> getContent() {
        return content;
    }

    public void setContent(List<DitaElement> content) {
        this.content = content;
    }

    public void addContent(DitaElement element) {
        this.content.add(element);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

