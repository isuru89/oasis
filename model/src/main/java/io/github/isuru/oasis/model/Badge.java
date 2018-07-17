package io.github.isuru.oasis.model;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class Badge implements Serializable {

    private final Badge parent;
    private final Long id;
    private final String name;

    public Badge(Long id, String name) {
        this(id, name, null);
    }

    public Badge(Long id, String name, Badge parent) {
        this.parent = parent;
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return "Badge{" +
                "name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }
}
