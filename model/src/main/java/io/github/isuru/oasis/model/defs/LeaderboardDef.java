package io.github.isuru.oasis.model.defs;

import java.util.List;

public class LeaderboardDef {

    private Long id;
    private String name;
    private String displayName;

    private List<String> ruleIds;
    private List<String> excludeRuleIds;
    private String orderBy;

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

    public List<String> getExcludeRuleIds() {
        return excludeRuleIds;
    }

    public void setExcludeRuleIds(List<String> excludeRuleIds) {
        this.excludeRuleIds = excludeRuleIds;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
