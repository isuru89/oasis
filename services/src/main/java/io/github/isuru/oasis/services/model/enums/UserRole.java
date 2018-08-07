package io.github.isuru.oasis.services.model.enums;

/**
 * @author iweerarathna
 */
public enum UserRole {

    ADMIN(1),

    CURATOR(2),

    PLAYER(8);

    private final int index;

    UserRole(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
