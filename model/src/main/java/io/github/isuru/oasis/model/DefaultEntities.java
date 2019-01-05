package io.github.isuru.oasis.model;

import io.github.isuru.oasis.model.defs.LeaderboardDef;

/**
 * @author iweerarathna
 */
public class DefaultEntities {

    public static final LeaderboardDef DEFAULT_LEADERBOARD_DEF;

    public static final String DEFAULT_TEAM_NAME = "default_Default";
    public static final String DEFAULT_TEAM_SCOPE_NAME = "Default";

    public static final String INTERNAL_EVENT_SOURCE_NAME = "oasis_internal";

    public static final String DEF_ADMIN_USER = "admin@oasis.com";
    public static final String DEF_CURATOR_USER = "curator@oasis.com";
    public static final String DEF_PLAYER_USER = "player@oasis.com";

    static {
        DEFAULT_LEADERBOARD_DEF = new LeaderboardDef();
        DEFAULT_LEADERBOARD_DEF.setName("Oasis_Leaderboard");
        DEFAULT_LEADERBOARD_DEF.setDisplayName("Oasis Leaderboard");
    }

}
