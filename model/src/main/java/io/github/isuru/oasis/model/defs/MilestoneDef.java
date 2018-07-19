package io.github.isuru.oasis.model.defs;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneDef {

    private Long id;
    private String name;
    private String displayName;
    private String from;
    private List<String> pointIds;
    private String event;
    private String aggregator;
    private String accumulator;
    private String condition;
    private String accumulatorType;
    private Map<Integer, Object> levels;
    private Map<Integer, Double> awardPoints;

    public Double getAwardPoints(int level) {
        if (awardPoints != null) {
            return awardPoints.get(level);
        }
        return null;
    }

    public void setAwardPoints(Map<Integer, Double> awardPoints) {
        this.awardPoints = awardPoints;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getPointIds() {
        return pointIds;
    }

    public void setPointIds(List<String> pointIds) {
        this.pointIds = pointIds;
    }

    public String getAccumulatorType() {
        return accumulatorType;
    }

    public void setAccumulatorType(String accumulatorType) {
        this.accumulatorType = accumulatorType;
    }

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(String accumulator) {
        this.accumulator = accumulator;
    }

    public Map<Integer, Object> getLevels() {
        return levels;
    }

    public void setLevels(Map<Integer, Object> levels) {
        this.levels = levels;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
