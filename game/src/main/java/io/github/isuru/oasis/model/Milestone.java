package io.github.isuru.oasis.model;

import io.github.isuru.oasis.process.AggregatorType;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class Milestone implements Serializable {

    private String id;
    private String event;
    private boolean realValues;
    private Serializable accumulatorExpr;
    private Serializable condition;
    private AggregatorType aggregator = AggregatorType.COUNT;
    private List<Level> levels;

    public boolean isRealValues() {
        return realValues;
    }

    public void setRealValues(boolean realValues) {
        this.realValues = realValues;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
