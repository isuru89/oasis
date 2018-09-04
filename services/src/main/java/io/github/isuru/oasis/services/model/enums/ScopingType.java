package io.github.isuru.oasis.services.model.enums;

public enum ScopingType {

    TEAM,
    TEAM_SCOPE,
    GLOBAL;

    public static ScopingType from(String text) {
        for (ScopingType scopingType : values()) {
            if (scopingType.name().equalsIgnoreCase(text)) {
                return scopingType;
            }
        }
        return null;
    }
}
