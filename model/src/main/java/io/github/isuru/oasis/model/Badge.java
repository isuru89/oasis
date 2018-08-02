package io.github.isuru.oasis.model;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class Badge implements Serializable {

    private Badge parent;
    private Long id;
    private Double awardPoints;
    private String name;

    public Badge() {
    }

    public Badge(Long id, String name) {
        this(id, name, null);
    }

    public Badge(Long id, String name, Badge parent) {
        this.parent = parent;
        this.id = id;
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Badge getParent() {
        return parent;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getAwardPoints() {
        return awardPoints;
    }

    public void setAwardPoints(Double awardPoints) {
        this.awardPoints = awardPoints;
    }

    @Override
    public String toString() {
        return "Badge{" +
                "name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }
}
