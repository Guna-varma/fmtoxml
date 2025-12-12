package com.cms.projects.transformation.dita.model;

import java.util.ArrayList;
import java.util.List;

public class DitaElement {
    private String type;
    private String content;
    private List<DitaElement> children;
    private String id;
    private String href;

    public DitaElement() {
        this.children = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<DitaElement> getChildren() {
        return children;
    }

    public void setChildren(List<DitaElement> children) {
        this.children = children;
    }

    public void addChild(DitaElement child) {
        this.children.add(child);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}

