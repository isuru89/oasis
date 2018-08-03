package io.github.isuru.oasis.services.model.enums;

/**
 * @author iweerarathna
 */
public enum LeaderboardType {

    CURRENT_DAY(false, "%x-%m-%d"),
    CURRENT_WEEK(false, "%x-%v"),
    CURRENT_MONTH(false, "%x-%m"),
    CUSTOM(true, "");

    private final boolean custom;
    private final String pattern;

    LeaderboardType(boolean special, String pattern) {
        this.custom = special;
        this.pattern = pattern;
    }

    public boolean isCustom() {
        return custom;
    }

    public String getPattern() {
        return pattern;
    }
}
