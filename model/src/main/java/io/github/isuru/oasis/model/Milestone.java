package io.github.isuru.oasis.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author iweerarathna
 */
public class Milestone implements Serializable {

    private long id;
    private String name;
    private String displayName;
    private String from;
    private Set<String> pointIds;
    private String event;
    private boolean realValues;
    private Serializable accumulatorExpr;
    private Serializable condition;
    private AggregatorType aggregator = AggregatorType.COUNT;
    private boolean onlyPositive = false;
    private List<Level> levels;
    private transient Map<Integer, Level> levelMap = null;
    private Long startingLevel = null;

    public Long getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(Long startingLevel) {
        this.startingLevel = startingLevel;
    }

    public boolean isOnlyPositive() {
        return onlyPositive;
    }

    public void setOnlyPositive(boolean onlyPositive) {
        this.onlyPositive = onlyPositive;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Set<String> getPointIds() {
        return pointIds;
    }

    public void setPointIds(Set<String> pointIds) {
        this.pointIds = pointIds;
    }

    public boolean isRealValues() {
        return realValues;
    }

    public void setRealValues(boolean realValues) {
        this.realValues = realValues;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public Level getLevel(int level) {
        if (levelMap == null) {
            setupCache();
        }
        return levelMap.get(level);
    }

    private void setupCache() {
        Map<Integer, Level> memo = new HashMap<>();
        for (Level level : levels) {
            memo.put(level.getLevel(), level);
        }
        this.levelMap = memo;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Serializable getAccumulatorExpr() {
        return accumulatorExpr;
    }

    public void setAccumulatorExpr(Serializable accumulatorExpr) {
        this.accumulatorExpr = accumulatorExpr;
    }

    public Serializable getCondition() {
        return condition;
    }

    public void setCondition(Serializable condition) {
        this.condition = condition;
    }

    public AggregatorType getAggregator() {
        return aggregator;
    }

    public void setAggregator(AggregatorType aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    public String toString() {
        return "Milestone=" + id;
    }

    public static class Level implements Serializable {
        private int level;
        private Number number;
        private Double awardPoints;

        public Double getAwardPoints() {
            return awardPoints;
        }

        public void setAwardPoints(Double awardPoints) {
            this.awardPoints = awardPoints;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public Number getNumber() {
            return number;
        }

        public void setNumber(Number number) {
            this.number = number;
        }
    }
}
