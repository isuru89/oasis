package io.github.isuru.oasis.model;

import java.io.Serializable;
import java.util.List;

public class Race implements Serializable {

    private Long id;
    private String name;
    private String displayName;

    private List<String> ruleIds;
    private AggregatorType aggregatorType;

    private String timewindow = "weekly";

    private Integer top;
    private Integer bottom;

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

    public List<String> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<String> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

    public void setAggregatorType(AggregatorType aggregatorType) {
        this.aggregatorType = aggregatorType;
    }

    public String getTimewindow() {
        return timewindow;
    }

    public void setTimewindow(String timewindow) {
        this.timewindow = timewindow;
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
