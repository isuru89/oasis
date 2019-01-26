package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.services.model.EventSourceToken;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.services.IEventsService;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IProfileService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DataCache {

    private static final Logger LOG = LoggerFactory.getLogger(DataCache.class);

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IEventsService eventsService;

    private String allUserTmpPassword;

    private long adminUserId;

    private final Map<Long, OasisGameDef> cache = new ConcurrentHashMap<>();
    private long defGameId;
    private TeamProfile teamDefault;
    private TeamScope teamScopeDefault;
    private EventSourceToken internalEventSourceToken;

    private LeaderboardDef defaultLeaderboard;

    void setup() throws Exception {
        allUserTmpPassword = RandomStringUtils.randomAlphanumeric(10);
        LOG.info(" *** Temporary password for all player authentication: " + allUserTmpPassword);

        List<GameDef> gameDefs = gameDefService.listGames();
        for (GameDef gameDef : gameDefs) {
            Long id = gameDef.getId();
            OasisGameDef oasisGameDef = loadGameDefs(id, gameDef);

            cache.put(id, oasisGameDef);
            defGameId = id;
        }


        teamScopeDefault = profileService.readTeamScope(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME);

        if (teamScopeDefault == null) {
            throw new IllegalStateException("Default team scope is not found in database! " +
                    "Run the bootstrap and make default team and team scope.");
        }

        List<TeamProfile> teamProfiles = profileService.listTeams(teamScopeDefault.getId());
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

        UserProfile userProfile = profileService.readUserProfile(DefaultEntities.DEF_ADMIN_USER);
        if (userProfile == null) {
            throw new IllegalStateException("No admin user is found on the system!");
        }
        adminUserId = userProfile.getId();

        // read default leaderboard definition
        String defLbName = DefaultEntities.DEFAULT_LEADERBOARD_DEF.getName();
        gameDefService.listLeaderboardDefs(defGameId).stream()
                .filter(l -> defLbName.equals(l.getName()))
                .findFirst()
                .ifPresent(l -> defaultLeaderboard = l);

        internalEventSourceToken = eventsService.readInternalSourceToken()
                .orElseThrow(() -> new IllegalStateException(
                        "Internal event source token is not found in database!" +
                        "Run the bootstrap and make default team and team scope."));
    }

    public long getAdminUserId() {
        return adminUserId;
    }

    public OasisGameDef loadGameDefs(long gameId) throws Exception {
        return loadGameDefs(gameId, null);
    }

    private OasisGameDef loadGameDefs(long gameId, GameDef def) throws Exception {
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

    public String getAllUserTmpPassword() {
        return allUserTmpPassword;
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

    public LeaderboardDef getDefaultLeaderboard() {
        return defaultLeaderboard;
    }
}
