package io.github.isuru.oasis.model.defs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaderboardDef extends BaseDef {

    public static final Set<String> ORDER_BY_ALLOWED = new HashSet<>(Arrays.asList("asc", "desc"));

    private List<String> ruleIds;
    private List<String> excludeRuleIds;
    private String orderBy;

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
