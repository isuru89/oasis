package io.github.isuru.oasis.model.rules;

import java.util.HashSet;
import java.util.Set;

/**
 * @author iweerarathna
 */
public class LeaderboardRule {

    private String id;
    private String name;
    private String motto;

    private final Set<String> pointIds = new HashSet<>();
    private boolean descendingOrder = true;

    public boolean isDescendingOrder() {
        return descendingOrder;
    }

    public void setDescendingOrder(boolean descendingOrder) {
        this.descendingOrder = descendingOrder;
    }

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

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public Set<String> getPointIds() {
        return pointIds;
    }
}
