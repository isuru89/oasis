package io.github.isuru.oasis.model;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class Badge implements Serializable {

    private final Badge parent;
    private final String id;

    public Badge(String id) {
        this(id, null);
    }

    public Badge(String id, Badge parent) {
        this.parent = parent;
        this.id = id;
    }

    public Badge getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Badge{" +
                "id='" + id + '\'' +
                ", parent=" + parent +
                '}';
    }
}
