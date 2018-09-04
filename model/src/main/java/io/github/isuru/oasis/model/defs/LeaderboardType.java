package io.github.isuru.oasis.model.defs;

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

    public static LeaderboardType from(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.startsWith("week")) {
            return LeaderboardType.CURRENT_WEEK;
        } else if (text.startsWith("da")) {
            return LeaderboardType.CURRENT_DAY;
        } else if (text.startsWith("mo")) {
            return LeaderboardType.CURRENT_MONTH;
        } else if (text.startsWith("custom")) {
            return LeaderboardType.CUSTOM;
        }
        return null;
    }
}
