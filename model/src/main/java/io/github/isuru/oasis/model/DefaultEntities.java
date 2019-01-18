package io.github.isuru.oasis.model;

import io.github.isuru.oasis.model.defs.LeaderboardDef;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author iweerarathna
 */
public class DefaultEntities {

    public static final LeaderboardDef DEFAULT_LEADERBOARD_DEF;

    public static final String DEFAULT_TEAM_NAME = "default.default";
    public static final String DEFAULT_TEAM_SCOPE_NAME = "Default";

    public static final String INTERNAL_EVENT_SOURCE_NAME = "oasis_internal";

    public static final String DEF_ADMIN_USER = "admin@oasis.com";
    public static final String DEF_CURATOR_USER = "curator@oasis.com";
    public static final String DEF_PLAYER_USER = "player@oasis.com";

    public static final Set<String> RESERVED_USERS = new HashSet<>(
            Arrays.asList(DEF_ADMIN_USER, DEF_PLAYER_USER, DEF_CURATOR_USER));

    static {
        DEFAULT_LEADERBOARD_DEF = new LeaderboardDef();
        DEFAULT_LEADERBOARD_DEF.setName("Oasis_Leaderboard");
        DEFAULT_LEADERBOARD_DEF.setDisplayName("Oasis Leaderboard");
    }

    public static String deriveDefaultTeamName(String teamScopeName) {
        return String.format("%s.default", teamScopeName);
    }

    public static String deriveDefScopeUser(String teamScopeName) {
        return String.format("default@%s.oasis.com", teamScopeName);
    }

    public static String deriveDefTeamUser(String teamName) {
        return String.format("user@%s.oasis.com", teamName);
    }
}
