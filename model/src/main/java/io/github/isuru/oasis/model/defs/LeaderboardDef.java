package io.github.isuru.oasis.model.defs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaderboardDef {

    public static final Set<String> ORDER_BY_ALLOWED = new HashSet<>(Arrays.asList("asc", "desc"));

    private Long id;
    private String name;
    private String displayName;
    private String description;

    private List<String> ruleIds;
    private List<String> excludeRuleIds;
    private String orderBy;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
