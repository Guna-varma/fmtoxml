package com.cms.projects.transformation.dita.model;

import java.util.ArrayList;
import java.util.List;

public class DitaMap {
    private String id;
    private String title;
    private List<DitaMapReference> references;
    private String fileName;

    public DitaMap() {
        this.references = new ArrayList<>();
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

    public List<DitaMapReference> getReferences() {
        return references;
    }

    public void setReferences(List<DitaMapReference> references) {
        this.references = references;
    }

    public void addReference(DitaMapReference reference) {
        this.references.add(reference);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static class DitaMapReference {
        private String href;
        private String format;
        private String type;
        private String title;
        private List<DitaMapReference> children;

        public DitaMapReference() {
            this.children = new ArrayList<>();
            this.format = "dita";
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<DitaMapReference> getChildren() {
            return children;
        }

        public void setChildren(List<DitaMapReference> children) {
            this.children = children;
        }

        public void addChild(DitaMapReference child) {
            this.children.add(child);
        }
    }
}

