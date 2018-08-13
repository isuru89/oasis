package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.utils.EventSourceToken;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public class DataCache {

    private IOasisApiService apiService;

    private final Map<Long, OasisGameDef> cache = new ConcurrentHashMap<>();
    private long defGameId;
    private TeamProfile teamDefault;
    private TeamScope teamScopeDefault;
    private EventSourceToken internalEventSourceToken;

    void setup(IOasisApiService apiService) throws Exception {
        this.apiService = apiService;

        IGameDefService gameDefService = apiService.getGameDefService();
        List<GameDef> gameDefs = gameDefService.listGames();
        for (GameDef gameDef : gameDefs) {
            Long id = gameDef.getId();
            OasisGameDef oasisGameDef = loadGameDefs(id, gameDef);

            cache.put(id, oasisGameDef);
            defGameId = id;
        }


        List<TeamScope> teamScopes = apiService.getProfileService().listTeamScopes();
        for (TeamScope scope : teamScopes) {
            if (scope.getName().equalsIgnoreCase(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME)) {
                teamScopeDefault = scope;
                break;
            }
        }

        if (teamScopeDefault == null) {
            throw new IllegalStateException("Default team scope is not found in database! " +
                    "Run the bootstrap and make default team and team scope.");
        }

        List<TeamProfile> teamProfiles = apiService.getProfileService().listTeams(teamScopeDefault.getId());
        for (TeamProfile profile : teamProfiles) {
            if (profile.getName().equalsIgnoreCase(DefaultEntities.DEFAULT_TEAM_NAME)) {
                teamDefault = profile;
                break;
            }
        }

        if (teamDefault == null) {
            throw new IllegalStateException("Default team is not found in database!" +
                    "Run the bootstrap and make default team and team scope.");
        }

        internalEventSourceToken = apiService.getEventService().readInternalSourceToken()
                .orElseThrow(() -> new IllegalStateException(
                        "Internal event source token is not found in database!" +
                        "Run the bootstrap and make default team and team scope."));
    }

    private OasisGameDef loadGameDefs(long gameId) throws Exception {
        return loadGameDefs(gameId, null);
    }

    private OasisGameDef loadGameDefs(long gameId, GameDef def) throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        GameDef gameDef = def;
        if (gameDef == null) {
            gameDef = gameDefService.readGame(gameId);
        }
        OasisGameDef oasisGameDef = new OasisGameDef();
        oasisGameDef.setGame(gameDef);

        oasisGameDef.setKpis(gameDefService.listKpiCalculations(gameId));
        oasisGameDef.setPoints(gameDefService.listPointDefs(gameId));
        oasisGameDef.setBadges(gameDefService.listBadgeDefs(gameId));
        oasisGameDef.setMilestones(gameDefService.listMilestoneDefs(gameId));
        return oasisGameDef;
    }

    public long getDefGameId() {
        return defGameId;
    }

    public TeamProfile getTeamDefault() {
        return teamDefault;
    }

    public TeamScope getTeamScopeDefault() {
        return teamScopeDefault;
    }

    public EventSourceToken getInternalEventSourceToken() {
        return internalEventSourceToken;
    }

    public int getGameCount() {
        return cache.size();
    }

    public static DataCache get() {
        return Holder.INSTANCE;
    }

    private DataCache() {}

    private static class Holder {
        private static final DataCache INSTANCE = new DataCache();
    }

}
