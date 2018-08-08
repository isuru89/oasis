package io.github.isuru.oasis.services.utils;

/**
 * @author iweerarathna
 */
public final class UserRole {

    public static final int ADMIN = 1;
    public static final int CURATOR = 2;
    //public static final int TEAM_LEADER = 4;
    public static final int PLAYER = 8;

    public static final int ALL_ROLE = ADMIN | CURATOR | PLAYER;

    public static boolean hasRole(int role, int roleType) {
        return role > 0 && (role & roleType) == roleType;
    }
}
