package io.github.isuru.oasis.model;

import io.github.isuru.oasis.model.utils.GroupingScope;

import java.io.Serializable;

public class Race implements Serializable {

    private Long id;
    private String name;
    private String displayName;

    private String event;
    private Serializable condition;
    private AggregatorType aggregatorType;
    private Serializable accumulator;

    private Double initialValue = 0.0;
    private String timewindow = "weekly";

    private GroupingScope groupingScope = GroupingScope.TEAM_SCOPE;

    private Integer top;
    private Integer bottom;

    public Double getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Double initialValue) {
        this.initialValue = initialValue;
    }

    public String getTimewindow() {
        return timewindow;
    }

    public void setTimewindow(String timewindow) {
        this.timewindow = timewindow;
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

    public Serializable getCondition() {
        return condition;
    }

    public void setCondition(Serializable condition) {
        this.condition = condition;
    }

    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

    public void setAggregatorType(AggregatorType aggregatorType) {
        this.aggregatorType = aggregatorType;
    }

    public Serializable getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(Serializable accumulator) {
        this.accumulator = accumulator;
    }

    public GroupingScope getGroupingScope() {
        return groupingScope;
    }

    public void setGroupingScope(GroupingScope groupingScope) {
        this.groupingScope = groupingScope;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getBottom() {
        return bottom;
    }

    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }
}
