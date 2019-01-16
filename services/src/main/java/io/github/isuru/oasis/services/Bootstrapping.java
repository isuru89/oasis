package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.model.EventSourceToken;
import io.github.isuru.oasis.services.model.SubmittedJob;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.services.IEventsService;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.ILifecycleService;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.services.LifecycleImplManager;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.model.UserRole;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Bootstrapping {

    private static final Logger LOG = LoggerFactory.getLogger(Bootstrapping.class);

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IEventsService eventsService;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private LifecycleImplManager lifecycleImplManager;

    @Autowired
    private IOasisDao dao;

    @Autowired
    private DataCache dataCache;

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() throws Exception {
        LOG.info("-------------------------------------------------------");
        LOG.info("OASIS - STARTUP / Initialized");
        LOG.info("-------------------------------------------------------");

        eventsService.init();

        initSystem(dao);

        // setup data cache
        LOG.info("  - Initializing cache from db...");
        dataCache.setup();

        LOG.info("-------------------------------------------------------");
        LOG.info("OASIS - STARTUP / Completed.");
        LOG.info("-------------------------------------------------------");
    }

    private void initSystem(IOasisDao dao) throws Exception {
        try {
            // add default team scope...
            TeamScope defTeamScope = addDefaultTeamScope();

            // add default team...
            TeamProfile defaultTeam = addDefaultTeam(defTeamScope);

            // add internal event source
            createInternalToken();

            // add users
            addUsers(defaultTeam.getId());

            // resume
            resumeGameAndChallenges();

        } catch (Throwable error) {
            // revert back to previous status...
            cleanTables(dao, "OA_EVENT_SOURCE", "OA_TEAM", "OA_TEAM_SCOPE", "OA_USER");
            throw error;
        }
    }

    private TeamScope addDefaultTeamScope() throws Exception {
        TeamScope defaultTeamScope = profileService.readTeamScope(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME);
        if (defaultTeamScope == null) {
            LOG.info("  - Default team scope does not exist. Creating...");
            TeamScope teamScope = new TeamScope();
            teamScope.setName(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME);
            teamScope.setDisplayName(DefaultEntities.DEFAULT_TEAM_SCOPE_NAME);
            long id = profileService.addTeamScope(teamScope);
            return profileService.readTeamScope(id);
        } else {
            LOG.info("  - Default team scope exists.");
            return defaultTeamScope;
        }
    }

    private TeamProfile addDefaultTeam(TeamScope defTeamScope) throws Exception {
        List<TeamProfile> defaultTeams = profileService.listTeams(defTeamScope.getId());
        if (defaultTeams.isEmpty()) {
            return addDefaultTeamProfile(profileService, defTeamScope);
        } else {
            Optional<TeamProfile> defTeam = defaultTeams.stream()
                    .filter(t -> DefaultEntities.DEFAULT_TEAM_NAME.equalsIgnoreCase(t.getName()))
                    .findFirst();
            if (!defTeam.isPresent()) {
                return addDefaultTeamProfile(profileService, defTeamScope);
            } else {
                LOG.info("  - Default team exists.");
                return defTeam.get();
            }
        }
    }

    private void createInternalToken() throws Exception {
        Optional<EventSourceToken> internalSourceToken = eventsService.readInternalSourceToken();
        if (!internalSourceToken.isPresent()) {
            LOG.info("  - Internal event source token does not exist. Creating...");
            EventSourceToken eventSourceToken = new EventSourceToken();
            eventSourceToken.setSourceName(DefaultEntities.INTERNAL_EVENT_SOURCE_NAME);
            eventSourceToken.setDisplayName(DefaultEntities.INTERNAL_EVENT_SOURCE_NAME);
            eventSourceToken.setInternal(true);
            eventsService.addEventSource(eventSourceToken);
        }
    }

    private void resumeGameAndChallenges() throws Exception {
        if (BooleanUtils.toBoolean(OasisUtils.getEnvOr(
                "OASIS_MANUAL_GAME_START", "oasis.manual.game.start", "no"))) {
            LOG.info("  - Detected manual game start env variable. No games will be automatically started!");
            return;
        }

        List<GameDef> gameDefs = gameDefService.listGames();
        if (gameDefs == null || gameDefs.isEmpty()) {
            // no active game exist. do nothing
            LOG.info("  - No active game exists. Nothing will be started.");
            return;
        }

        ILifecycleService lifecycleService = lifecycleImplManager.get();

        // resume game first...
        List<Integer> resumedGameIds = new LinkedList<>();
        for (GameDef gameDef : gameDefs) {
            LOG.info("  - Resuming game: {}", gameDef.getName());
            lifecycleService.resumeGame(gameDef.getId());
            resumedGameIds.add(gameDef.getId().intValue());
        }

        // resume previously running challenges...
        Iterable<SubmittedJob> runningJobs = dao.executeQuery("jobs/getHadRunningJobs",
                Maps.create("currentTime", System.currentTimeMillis()),
                SubmittedJob.class);
        for (SubmittedJob job : runningJobs) {
            if (!resumedGameIds.contains(job.getDefId())) {
                LOG.info("  - Resuming challenge: {}", job.getDefId());
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
        UserProfile adminUser = profileService.readUserProfile(DefaultEntities.DEF_ADMIN_USER);
        if (adminUser == null) {
            LOG.info("  - Admin user does not exist. Creating...");
            UserProfile admin = new UserProfile();
            admin.setEmail(DefaultEntities.DEF_ADMIN_USER);
            admin.setName("Admin");
            admin.setActivated(true);
            long adminId = profileService.addUserProfile(admin);
            profileService.addUserToTeam(adminId, defTeamId, UserRole.ADMIN);
        } else {
            LOG.info("  - Admin user exists.");
        }

        UserProfile curatorUser = profileService.readUserProfile(DefaultEntities.DEF_CURATOR_USER);
        if (curatorUser == null) {
            LOG.info("  - Curator user does not exist. Creating...");
            UserProfile curator = new UserProfile();
            curator.setEmail(DefaultEntities.DEF_CURATOR_USER);
            curator.setName("Curator");
            curator.setActivated(true);
            long curatorId = profileService.addUserProfile(curator);
            profileService.addUserToTeam(curatorId, defTeamId, UserRole.CURATOR);
        } else {
            LOG.info("  - Curator user exists.");
        }

        UserProfile playerUser = profileService.readUserProfile(DefaultEntities.DEF_PLAYER_USER);
        if (playerUser == null) {
            LOG.info("  - Player user does not exist. Creating...");
            UserProfile player = new UserProfile();
            player.setEmail(DefaultEntities.DEF_PLAYER_USER);
            player.setName("Player");
            player.setActivated(true);
            long playerId = profileService.addUserProfile(player);
            profileService.addUserToTeam(playerId, defTeamId, UserRole.PLAYER);
        } else {
            LOG.info("  - Player user exists.");
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

    private static TeamProfile addDefaultTeamProfile(IProfileService profileService, TeamScope teamScope) throws Exception {
        LOG.info("  - Default team does not exist. Creating...");

        TeamProfile profile = new TeamProfile();
        profile.setName(DefaultEntities.DEFAULT_TEAM_NAME);
        profile.setTeamScope(teamScope.getId());
        long id = profileService.addTeam(profile);
        return profileService.readTeam(id);
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
