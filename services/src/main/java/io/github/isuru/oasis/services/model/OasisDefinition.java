package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public enum OasisDefinition {

    GAME(1),
    KPI(4),
    POINT(5),
    BADGE(6),
    MILESTONE(7),
    LEADERBOARD(8);

    private final int typeId;

    OasisDefinition(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
