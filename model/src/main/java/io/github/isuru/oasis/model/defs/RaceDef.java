package io.github.isuru.oasis.model.defs;

import java.util.List;

public class RaceDef extends BaseDef {

    private String fromScope;
    private List<String> ruleIds;
    private String aggregatorType;

    private String timewindow = "weekly";

    private Integer top;
    private Integer bottom;

    public String getFromScope() {
        return fromScope;
    }

    public void setFromScope(String fromScope) {
        this.fromScope = fromScope;
    }

    public List<String> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<String> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public String getAggregatorType() {
        return aggregatorType;
    }

    public void setAggregatorType(String aggregatorType) {
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
