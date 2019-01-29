package io.github.isuru.oasis.services.services;

final class Q {

    static class JOBS {
        private static final String PATH = "jobs/";

        static final String SUBMIT_JOB = PATH + "submitJob";
        static final String UPDATE_JOB = PATH + "updateJob";
        static final String STOP_JOB = PATH + "stopJob";
        static final String STOP_JOB_BY_DEF = PATH + "stopJobByDefId";
        static final String GET_JOB = PATH + "getJobOfDef";
        static final String GET_HAD_RUNNING_JOBS = PATH + "getHadRunningJobs";
    }

    static class EVENTS {
        private static final String PATH = "events/";

        static final String ADD_EVENT_SOURCE = PATH + "addEventSource";
        static final String DISABLE_EVENT_SOURCE = PATH + "disableEventSource";
        static final String LIST_ALL_EVENT_SOURCES = PATH + "listAllEventSources";
        static final String UPDATE_AS_DOWNLOADED = PATH + "updateAsDownloaded";
        static final String READ_EVENT_SOURCE = PATH + "readEventSource";

    }

    static class METAPHOR {
        private static final String PATH = "metaphor/";

        static final String ADD_SHOP_ITEM = PATH + "addShopItem";
        static final String BUY_ITEM = PATH + "buyItem";
        static final String DISABLE_ITEM = PATH + "disableItem";
        static final String DISABLE_PURCHASES_OF_ITEM = PATH + "disablePurchasesOfItem";
        static final String DISABLE_PURCHASES_OF_USER = PATH + "disablePurchasesOfUser";
        static final String ITEM_HERO_BUYABLE = PATH + "itemHeroBuyable";
        static final String ITEM_HERO_SHARABLE = PATH + "itemHeroSharable";
        static final String LIST_HEROS = PATH + "listHeros";
        static final String LIST_ITEMS = PATH + "listItems";
        static final String LIST_ITEMS_FOR_HERO = PATH + "listItemsForHero";
        static final String READ_ITEM = PATH + "readItem";
        static final String REAVAILABLE_PURCHASES_OF_USER = PATH + "reavailablePurchasesOfUser";
        static final String SHARE_ITEM = PATH + "shareItem";
        static final String SHARE_TO_ITEM = PATH + "shareToItem";
        static final String UPDATE_ITEM_AVAILABILITY = PATH + "updateItemAvail";
        static final String UPDATE_HERO = PATH + "updateHero";
    }

    static class PROFILE {
        static final String PATH = "profile/";

        static final String ADD_TEAM = PATH + "addTeam";
        static final String ADD_TEAMSCOPE = PATH + "addTeamScope";
        static final String ADD_USER = PATH + "addUser";
        static final String ADD_USER_TO_TEAM = PATH + "addUserToTeam";
        static final String APPROVE_USER_TO_TEAM = PATH + "approveUserToTeam";
        static final String DEALLOCATE_FROM_TEAM = PATH + "deallocateFromTeam";
        static final String DISABLE_USER = PATH + "disableUser";
        static final String EDIT_TEAM = PATH + "editTeam";
        static final String EDIT_TEAMSCOPE = PATH + "editTeamScope";
        static final String EDIT_USER = PATH + "editUser";
        static final String FIND_CURRENT_TEAM_OF_USER = PATH + "findCurrentTeamOfUser";
        static final String FIND_SCOPE_BY_NAME = PATH + "findScopeByName";
        static final String FIND_TEAM_BY_NAME = PATH + "findTeamByName";
        static final String LIST_TEAMS_OF_SCOPE = PATH + "listTeamOfScope";
        static final String LIST_TEAM_SCOPES = PATH + "listTeamScopes";
        static final String LIST_USER_COUNT_OF_TEAMS = PATH + "listUserCountOfTeams";
        static final String LIST_USER_COUNT_OF_TEAMSCOPE = PATH + "listUserCountOfTeamScope";
        static final String LIST_USERS_OF_TEAM = PATH + "listUsersOfTeam";
        static final String LOGOUT_USER = PATH + "logoutUser";
        static final String READ_TEAM = PATH + "readTeam";
        static final String READ_TEAMSCOPE = PATH + "readTeamScope";
        static final String READ_USER = PATH + "readUser";
        static final String READ_USER_BY_EMAIL = PATH + "readUserByEmail";
        static final String READ_USER_BY_EXTID = PATH + "readUserByExtId";
        static final String REJECT_USER_IN_TEAM = PATH + "rejectUserInTeam";
        static final String SEARCH_USER = PATH + "searchUser";

    }

    static class DEF {
        private static final String PATH = "def/";
        private static final String ATTR = PATH + "attr/";

        static final String ADD_ATTRIBUTE = ATTR + "addAttributes";
        static final String ADD_DEF_ATTRIBUTE = ATTR + "addDefAttribute";
        static final String DISABLE_DEF_ATTRIBUTES = ATTR + "disableDefAttributes";
        static final String LIST_ATTRIBUTE = ATTR + "listAttributes";
        static final String LIST_DEF_ATTRIBUTE = ATTR + "listDefAttributes";
        static final String LIST_ALL_DEF_ATTRIBUTE = ATTR + "listAllDefAttributes";
        static final String LIST_DEF_ATTRIBUTE_GAME = ATTR + "listDefAttributesOfGame";
    }

    static class GAME {
        private static final String PATH = "game/";
        private static final String BATCH = PATH + "batch/";

        static final String ADD_RACE_AWARD = BATCH + "addRaceAward";
    }

    static class LEADERBOARD {
        static final String GLOBAL_LEADERBOARD = "leaderbaord/globalLeaderboard";
        static final String RACE_GLOBAL_LEADERBOARD = "leaderbaord/raceGlobalLeaderboard";
        static final String RACE_TEAM_LEADERBOARD = "leaderbaord/raceTeamLeaderboard";
        static final String TEAM_LEADERBOARD = "leaderbaord/teamLeaderboard";
    }

    static class STATS {
        static final String GET_CHALLENGE_WINNERS = "stats/getChallengeWinners";
        static final String GET_USER_POINTS_LIST = "stats/getUserPointsList";
        static final String GET_USER_BADGES_LIST = "stats/getUserBadgesList";
        static final String GET_USER_STAT_SUMMARY = "stats/getUserStatSummary";
        static final String GET_PURCHASED_ITEMS = "stats/getPurchasedItems";
        static final String GET_USER_BADGE_STAT = "stats/getUserBadgesStat";
        static final String GET_USER_MILESTONE_STAT = "stats/getUserMilestoneStat";
        static final String GET_USER_TEAM_RANKING = "stats/getUserTeamRanking";
        static final String TEAM_WISE_SUMMARY_STATS = "stats/teamWiseSummaryStats";
        static final String GET_USER_STATE_VALUES = "stats/getUserStateValues";
        static final String GET_USER_AVAILABLE_POINTS = "stats/getUserAvailablePoints";
    }

}
