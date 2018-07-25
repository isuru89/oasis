package io.github.isuru.oasis.model.defs;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author iweerarathna
 */
public class ChallengeDef implements Serializable {

    private Long id;
    private String name;
    private String displayName;

    private long expireAfter;
    private long startAt;
    private long winnerCount;

    private double points;

    private Collection<String> forEvents;
    private List<Serializable> conditions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
    }

    public long getStartAt() {
        return startAt;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public long getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(long winnerCount) {
        this.winnerCount = winnerCount;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Collection<String> getForEvents() {
        return forEvents;
    }

    public void setForEvents(Collection<String> forEvents) {
        this.forEvents = forEvents;
    }

    public List<Serializable> getConditions() {
        return conditions;
    }

    public void setConditions(List<Serializable> conditions) {
        this.conditions = conditions;
    }
}
