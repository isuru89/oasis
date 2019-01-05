package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.services.*;
import io.github.isuru.oasis.services.model.*;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
public class Bootstrapping {

    private static final Logger LOG = LoggerFactory.getLogger(Bootstrapping.class);

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IEventsService eventsService;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private ILifecycleService lifecycleService;

    @Autowired
    private IOasisDao dao;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() throws Exception {
        LOG.info("-------------------------------------------------------");
        LOG.info("OASIS - STARTUP / Initialized");
        LOG.info("-------------------------------------------------------");
        initSystem(dao);

        // setup data cache
        DataCache.get().setup(gameDefService, profileService, eventsService);

        LOG.info("-------------------------------------------------------");
        LOG.info("OASIS - STARTUP / Completed.");
        LOG.info("-------------------------------------------------------");
    }

    private void initSystem(IOasisDao dao) throws Exception {
        try {
            // add default team scope...
            List<TeamScope> teamScopes = profileService.listTeamScopes();
            TeamScope defTeamScope;
            if (teamScopes.isEmpty()) {
                defTeamScope = addDefaultTeamScope(profileService);
            } else {
                Optional<TeamScope> defTeamScopeOpt = teamScopes.stream()
                        .filter(ts -> DefaultEntities.DEFAULT_TEAM_SCOPE_NAME.equalsIgnoreCase(ts.getName()))
                        .findFirst();
                if (defTeamScopeOpt.isPresent()) {
                    defTeamScope = defTeamScopeOpt.get();
                } else {
                    defTeamScope = addDefaultTeamScope(profileService);
                }
            }

            // add default team...
            List<TeamProfile> defaultTeams = profileService.listTeams(defTeamScope.getId());
            long defTeamId = -1;
            if (defaultTeams.isEmpty()) {
                addDefaultTeamProfile(profileService, defTeamScope);
            } else {
                Optional<TeamProfile> defTeam = defaultTeams.stream()
                        .filter(t -> DefaultEntities.DEFAULT_TEAM_NAME.equalsIgnoreCase(t.getName()))
                        .findFirst();
                if (!defTeam.isPresent()) {
                    defTeamId = addDefaultTeamProfile(profileService, defTeamScope);
                } else {
                    defTeamId = defTeam.get().getId();
                }
            }

            // add internal event source
            Optional<EventSourceToken> internalSourceToken = eventsService.readInternalSourceToken();
            if (!internalSourceToken.isPresent()) {
                EventSourceToken eventSourceToken = new EventSourceToken();
                eventSourceToken.setSourceName(DefaultEntities.INTERNAL_EVENT_SOURCE_NAME);
                eventSourceToken.setDisplayName(DefaultEntities.INTERNAL_EVENT_SOURCE_NAME);
                eventSourceToken.setInternal(true);
                eventsService.addEventSource(eventSourceToken);
            }

            // add users
            addUsers(defTeamId);

            // resume
            resumeGameAndChallenges();

        } catch (Throwable error) {
            // revert back to previous status...
            cleanTables(dao, "OA_EVENT_SOURCE", "OA_TEAM", "OA_TEAM_SCOPE", "OA_USER");
            throw error;
        }
    }

    private void resumeGameAndChallenges() throws Exception {
        List<GameDef> gameDefs = gameDefService.listGames();
        if (gameDefs == null || gameDefs.isEmpty()) {
            // no active game exist. do nothing
            return;
        }

        // resume game first...
        List<Integer> resumedGameIds = new LinkedList<>();
        for (GameDef gameDef : gameDefs) {
            lifecycleService.resumeGame(gameDef.getId());
            resumedGameIds.add(gameDef.getId().intValue());
        }

        // resume previously running challenges...
        Iterable<SubmittedJob> runningJobs = dao.executeQuery("jobs/getHadRunningJobs",
                Maps.create("currentTime", System.currentTimeMillis()),
                SubmittedJob.class);
        for (SubmittedJob job : runningJobs) {
            if (!resumedGameIds.contains(job.getDefId())) {
                lifecycleService.resumeChallenge(job.getDefId());
            }
        }
    }

    private static void cleanTables(IOasisDao dao, String... tableNames) throws Exception {
        if (tableNames != null) {
            for (String tbl : tableNames) {
                dao.executeRawCommand("TRUNCATE " + tbl, null);
            }
        }
    }

    private void addUsers(long defTeamId) throws Exception {
        UserProfile adminUser = profileService.readUserProfile("admin@oasis.com");
        if (adminUser == null) {
            UserProfile admin = new UserProfile();
            admin.setEmail("admin@oasis.com");
            admin.setName("Admin");
            long adminId = profileService.addUserProfile(admin);
            profileService.addUserToTeam(adminId, defTeamId, UserRole.ADMIN);
        }

        UserProfile curatorUser = profileService.readUserProfile("curator@oasis.com");
        if (curatorUser == null) {
            UserProfile curator = new UserProfile();
            curator.setEmail("curator@oasis.com");
            curator.setName("Curator");
            long curatorId = profileService.addUserProfile(curator);
            profileService.addUserToTeam(curatorId, defTeamId, UserRole.CURATOR);
        }

        UserProfile playerUser = profileService.readUserProfile("player@oasis.com");
        if (playerUser == null) {
            UserProfile player = new UserProfile();
            player.setEmail("player@oasis.com");
            player.setName("Player");
            long playerId = profileService.addUserProfile(player);
            profileService.addUserToTeam(playerId, defTeamId, UserRole.PLAYER);
        }
    }

    public static void initGame(IGameDefService gameDefService, long gameId, GameOptionsDto optionsDto) throws Exception {
        addDefaultPointRules(gameDefService, gameId, optionsDto);

        // add default leaderboard definitions...
        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs(gameId);
        if (leaderboardDefs.isEmpty()) { // no leaderboard defs yet...
            addDefaultLeaderboards(gameDefService, gameId);
        }
    }

    private static long addDefaultTeamProfile(IProfileService profileService, TeamScope teamScope) throws Exception {
        TeamProfile profile = new TeamProfile();
        profile.setName(DefaultEntities.DEFAULT_TEAM_NAME);
        profile.setTeamScope(teamScope.getId());
        return profileService.addTeam(profile);
    }

    private static TeamScope addDefaultTeamScope(IProfileService profileService) throws Exception {
        TeamScope teamScope = new TeamScope();
        teamScope.setName(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME);
        teamScope.setDisplayName(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME);
        long id = profileService.addTeamScope(teamScope);
        return profileService.readTeamScope(id);
    }

    private static void addDefaultLeaderboards(IGameDefService defService, long gameId) throws Exception {
        defService.addLeaderboardDef(gameId, DefaultEntities.DEFAULT_LEADERBOARD_DEF);
    }

    private static void addDefaultPointRules(IGameDefService gameDefService, long gameId, GameOptionsDto optionsDto) throws Exception {
        if (optionsDto.isAllowPointCompensation()) {
            // add compensation point event
            PointDef compDef = new PointDef();
            compDef.setName(EventNames.POINT_RULE_COMPENSATION_NAME);
            compDef.setDisplayName("Rule to compensate points at any time.");
            compDef.setAmount("amount");
            compDef.setEvent(EventNames.EVENT_COMPENSATE_POINTS);
            compDef.setCondition("true");
            gameDefService.addPointDef(gameId, compDef);
        }

        if (optionsDto.isAwardPointsForMilestoneCompletion()) {
            PointDef msCompleteDef = new PointDef();
            msCompleteDef.setName(EventNames.POINT_RULE_MILESTONE_BONUS_NAME);
            msCompleteDef.setDisplayName("Award points when certain milestones are completed.");
            msCompleteDef.setAmount(optionsDto.getDefaultBonusPointsForMilestone());
            gameDefService.addPointDef(gameId, msCompleteDef);
        }

        if (optionsDto.isAwardPointsForBadges()) {
            PointDef bdgCompleteDef = new PointDef();
            bdgCompleteDef.setName(EventNames.POINT_RULE_BADGE_BONUS_NAME);
            bdgCompleteDef.setDisplayName("Award points when certain badges are completed.");
            bdgCompleteDef.setAmount(optionsDto.getDefaultBonusPointsForBadge());
            gameDefService.addPointDef(gameId, bdgCompleteDef);
        }

        {
            // add race point award rule...
            PointDef raceAwardDef = new PointDef();
            raceAwardDef.setName(EventNames.POINT_RULE_RACE_POINTS);
            raceAwardDef.setDisplayName("Rule to calculate points awarded from races.");
            raceAwardDef.setAmount("amount");
            raceAwardDef.setEvent(EventNames.EVENT_AWARD_BADGE);
            raceAwardDef.setCondition("true");
            gameDefService.addPointDef(gameId, raceAwardDef);
        }
    }

}
