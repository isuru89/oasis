package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.model.GameOptionsDto;
import io.github.isuru.oasis.services.model.SubmittedJob;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.RUtils;
import io.github.isuru.oasis.services.utils.UserRole;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Bootstrapping {

    static void initSystem(IOasisApiService apiService, IOasisDao dao) throws Exception {
        try {
            IProfileService profileService = apiService.getProfileService();

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
            Optional<EventSourceToken> internalSourceToken = apiService.getEventService().readInternalSourceToken();
            if (!internalSourceToken.isPresent()) {
                EventSourceToken eventSourceToken = new EventSourceToken();
                eventSourceToken.setDisplayName(DefaultEntities.INTERNAL_EVENT_SOURCE_NAME);
                eventSourceToken.setInternal(true);
                apiService.getEventService().addEventSource(eventSourceToken);
            }

            // add users
            addUsers(apiService, defTeamId);

            // resume
            resumeGameAndChallenges(apiService, dao);

        } catch (Throwable error) {
            // revert back to previous status...
            cleanTables(dao, "OA_EVENT_SOURCE", "OA_TEAM", "OA_TEAM_SCOPE", "OA_USER");
            throw error;
        }
    }

    private static void resumeGameAndChallenges(IOasisApiService apiService, IOasisDao dao) throws Exception {
        List<GameDef> gameDefs = apiService.getGameDefService().listGames();
        if (gameDefs == null || gameDefs.isEmpty()) {
            // no active game exist. do nothing
            return;
        }

        // resume game first...
        List<Integer> resumedGameIds = new LinkedList<>();
        for (GameDef gameDef : gameDefs) {
            apiService.getLifecycleService().resumeGame(gameDef.getId());
            resumedGameIds.add(gameDef.getId().intValue());
        }

        // resume previously running challenges...
        Iterable<SubmittedJob> runningJobs = dao.executeQuery("jobs/getHadRunningJobs",
                Maps.create("currentTime", System.currentTimeMillis()),
                SubmittedJob.class);
        for (SubmittedJob job : runningJobs) {
            if (!resumedGameIds.contains(job.getDefId())) {
                apiService.getLifecycleService().resumeChallenge(job.getDefId());
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

    private static void addUsers(IOasisApiService apiService, long defTeamId) throws Exception {
        List<UserProfile> userProfiles = apiService.getProfileService()
                .listUsers(defTeamId, 0, 5000);

        if (userProfiles.stream().noneMatch(u -> u.getEmail().equals("admin@oasis.com"))) {
            UserProfile admin = new UserProfile();
            admin.setEmail("admin@oasis.com");
            admin.setName("Admin");
            long adminId = apiService.getProfileService().addUserProfile(admin);
            apiService.getProfileService().addUserToTeam(adminId, defTeamId, UserRole.ADMIN);
        }

        if (userProfiles.stream().noneMatch(u -> u.getEmail().equals("curator@oasis.com"))) {
            UserProfile curator = new UserProfile();
            curator.setEmail("curator@oasis.com");
            curator.setName("Curator");
            long curatorId = apiService.getProfileService().addUserProfile(curator);
            apiService.getProfileService().addUserToTeam(curatorId, defTeamId, UserRole.CURATOR);
        }

        if (userProfiles.stream().noneMatch(u -> u.getEmail().equals("player@oasis.com"))) {
            UserProfile player = new UserProfile();
            player.setEmail("player@oasis.com");
            player.setName("Player");
            long playerId = apiService.getProfileService().addUserProfile(player);
            apiService.getProfileService().addUserToTeam(playerId, defTeamId, UserRole.PLAYER);
        }
    }

    public static void initGame(IOasisApiService apiService, long gameId, GameOptionsDto optionsDto) throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();

        addDefaultPointRules(gameDefService, gameId, optionsDto);

        // add default leaderboard definitions...
        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs(gameId);
        if (leaderboardDefs.isEmpty()) { // no leaderboard defs yet...
            addDefaultLeaderboards(gameDefService, gameId, optionsDto);
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

    private static void addDefaultLeaderboards(IGameDefService defService, long gameId, GameOptionsDto optionsDto) throws Exception {
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
    }

}
