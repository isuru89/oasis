package io.github.isuru.oasis.services.model;

public class FeatureAttr {

    private Integer id;

    private String name;
    private String displayName;

    private int priority;

    public FeatureAttr() {
    }

    public FeatureAttr(String name, String displayName, int priority) {
        this.name = name;
        this.displayName = displayName;
        this.priority = priority;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
