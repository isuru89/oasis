package io.github.isuru.oasis.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.GameOptionsDto;

import java.util.List;

public class Bootstrapping {

    public static void initGame(IOasisApiService apiService, long gameId, GameOptionsDto optionsDto) throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();

        addDefaultPointRules(gameDefService, gameId, optionsDto);

        // add default leaderboard definitions...
        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs(gameId);
        if (leaderboardDefs.isEmpty()) { // no leaderboard defs yet...
            addDefaultLeaderboards(gameDefService, gameId, optionsDto);
        }
    }

    private static void addDefaultLeaderboards(IGameDefService defService, long gameId, GameOptionsDto optionsDto) throws Exception {
        LeaderboardDef dlb = new LeaderboardDef();
        dlb.setName("Oasis_AllRules_Leaderboard");
        dlb.setDisplayName("Oasis Leaderboard");
        defService.addLeaderboardDef(gameId, dlb);
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
