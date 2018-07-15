package com.virtusa.gto.oasis.services.backend.model;

import java.util.List;

public class FlinkJar {

    private String id;
    private String name;
    private Integer uploaded;
    private List<JarEntry> entry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUploaded() {
        return uploaded;
    }

    public void setUploaded(Integer uploaded) {
        this.uploaded = uploaded;
    }

    public List<JarEntry> getEntry() {
        return entry;
    }

    public void setEntry(List<JarEntry> entry) {
        this.entry = entry;
    }

    public static class JarEntry {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
