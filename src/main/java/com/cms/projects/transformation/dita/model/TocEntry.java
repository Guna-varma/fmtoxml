package com.cms.projects.transformation.dita.model;

import java.util.ArrayList;
import java.util.List;

public class TocEntry {
    private String title;
    private String href;
    private String type;
    private List<TocEntry> children;
    private int level;

    public TocEntry() {
        this.children = new ArrayList<>();
        this.level = 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TocEntry> getChildren() {
        return children;
    }

    public void setChildren(List<TocEntry> children) {
        this.children = children;
    }

    public void addChild(TocEntry child) {
        this.children.add(child);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

