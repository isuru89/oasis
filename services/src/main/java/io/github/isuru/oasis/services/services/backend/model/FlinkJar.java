package io.github.isuru.oasis.services.services.backend.model;

import java.util.List;

public class FlinkJar {

    private String id;
    private String name;
    private Long uploaded;
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

    public Long getUploaded() {
        return uploaded;
    }

    public void setUploaded(Long uploaded) {
        this.uploaded = uploaded;
    }

    public List<JarEntry> getEntry() {
        return entry;
    }

    public void setEntry(List<JarEntry> entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return "FlinkJar{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", uploaded=" + uploaded +
                ", entry=" + entry +
                '}';
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

        @Override
        public String toString() {
            return "JarEntry{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
