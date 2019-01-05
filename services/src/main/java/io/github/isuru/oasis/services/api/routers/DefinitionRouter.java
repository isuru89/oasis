package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IMetaphorService;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.AddGameDto;
import io.github.isuru.oasis.services.utils.OasisOptions;
import io.github.isuru.oasis.services.utils.UserRole;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class DefinitionRouter extends BaseRouters {

    private static final String P_GAME_ID = "gameId";
    private static final String POINT_ID = "pointId";
    private static final String BADGE_ID = "badgeId";
    private static final String MILESTONE_ID = "milestoneId";
    private static final String ITEM_ID = "itemId";
    private static final String STATE_ID = "stateId";
    private static final String CHALLENGE_ID = "challengeId";
    private static final String KPI_ID = "kpiId";
    private static final String BOARD_ID = "boardId";

    DefinitionRouter(IOasisApiService apiService, OasisOptions oasisOptions) {
        super(apiService, oasisOptions);
    }

    @Override
    public void register() {
        IGameDefService gds = getApiService().getGameDefService();
        IMetaphorService metaphorService = getApiService().getMetaphorService();

        post("/game", (req, res) -> {
            AddGameDto addGameDto = bodyAs(req, AddGameDto.class);
            return asResAdd(gds.createGame(addGameDto.getDef(), addGameDto.getOptions()));
        }, UserRole.ADMIN);

        Spark.path("/game", () -> {
            get("/all", (req, res) -> gds.listGames())
            .get("/heros", (req, res) -> metaphorService.listHeros())
            .get("/:gameId", (req, res) -> gds.readGame(asPLong(req, P_GAME_ID)))
            .delete("/:gameId",
                    (req, res) -> asResBool(gds.disableGame(asPLong(req, P_GAME_ID))), UserRole.ADMIN);

            post("/:gameId/kpi", (req, res) -> asResAdd(gds.addKpiCalculation(asPLong(req, P_GAME_ID), bodyAs(req, KpiDef.class))), UserRole.ADMIN);
            post("/:gameId/point", (req, res) -> asResAdd(gds.addPointDef(asPLong(req, P_GAME_ID), bodyAs(req, PointDef.class))), UserRole.ADMIN);
            post("/:gameId/badge", (req, res) -> asResAdd(gds.addBadgeDef(asPLong(req, P_GAME_ID), bodyAs(req, BadgeDef.class))), UserRole.ADMIN);
            post("/:gameId/milestone", (req, res) -> asResAdd(gds.addMilestoneDef(asPLong(req, P_GAME_ID), bodyAs(req, MilestoneDef.class))), UserRole.ADMIN);
            post("/:gameId/leaderboard", (req, res) -> asResAdd(gds.addLeaderboardDef(asPLong(req, P_GAME_ID), bodyAs(req, LeaderboardDef.class))), UserRole.CURATOR);
            post("/:gameId/challenge", (req, res) -> asResAdd(gds.addChallenge(asPLong(req, P_GAME_ID), bodyAs(req, ChallengeDef.class))), UserRole.ADMIN);
            post("/:gameId/item", (req, res) -> asResAdd(metaphorService.addShopItem(asPLong(req, P_GAME_ID), bodyAs(req, ShopItem.class))), UserRole.ADMIN);
            post("/:gameId/state", (req, res) -> asResAdd(gds.addStatePlay(asPLong(req, P_GAME_ID), bodyAs(req, StateDef.class))), UserRole.ADMIN);

            Spark.path("/:gameId/kpi", () -> {
                get("/all", (req, res) -> gds.listKpiCalculations(asPLong(req, P_GAME_ID)))
                .get("/:kpiId", (req, res) -> gds.readKpiCalculation(asPLong(req, KPI_ID)))
                .delete("/:kpiId", (req, res) ->
                        asResBool(gds.disableKpiCalculation(asPLong(req, KPI_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/point", () -> {
                get("/all", (req, res) -> gds.listPointDefs(asPLong(req, P_GAME_ID)))
                .get("/:pointId", (req, res) -> gds.readPointDef(asPLong(req, POINT_ID)))
                .delete("/:pointId", (req, res) ->
                        asResBool(gds.disablePointDef(asPLong(req, POINT_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/badge", () -> {
                get("/all", (req, res) -> gds.listBadgeDefs(asPLong(req, P_GAME_ID)))
                .get("/:badgeId", (req, res) -> gds.readBadgeDef(asPLong(req, BADGE_ID)))
                .delete("/:badgeId", (req, res) ->
                        asResBool(gds.disableBadgeDef(asPLong(req, BADGE_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/milestone", () -> {
                get("/all", (req, res) -> gds.listMilestoneDefs(asPLong(req, P_GAME_ID)))
                .get("/:milestoneId", (req, res) -> gds.readMilestoneDef(asPLong(req, MILESTONE_ID)))
                .delete("/:milestoneId", (req, res) ->
                        asResBool(gds.disableMilestoneDef(asPLong(req, MILESTONE_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/leaderboard", () -> {
                get("/all", (req, res) -> gds.listLeaderboardDefs(asPLong(req, P_GAME_ID)))
                .get("/:boardId", (req, res) -> gds.readLeaderboardDef(asPLong(req, BOARD_ID)))
                .delete("/:boardId", (req, res) ->
                        asResBool(gds.disableLeaderboardDef(asPLong(req, BOARD_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/challenge", () -> {
                get("/all", (req, res) -> gds.listChallenges(asPLong(req, P_GAME_ID)))
                .get("/:challengeId", (req, res) -> gds.readChallenge(asPLong(req, CHALLENGE_ID)))
                .delete("/:challengeId", (req, res) ->
                        asResBool(gds.disableChallenge(asPLong(req, CHALLENGE_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/item", () -> {
                get("/all", (req, res) -> metaphorService.listShopItems(asPLong(req, P_GAME_ID)))
                .get("/all/:heroId", (req, res) -> metaphorService.listShopItems(asPLong(req, P_GAME_ID), asPInt(req, "heroId")))
                .get("/:itemId", (req, res) -> metaphorService.readShopItem(asPLong(req, ITEM_ID)))
                .delete("/:itemId", (req, res) ->
                        asResBool(metaphorService.disableShopItem(asPLong(req, ITEM_ID))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/state", () -> {
                get("/all", (req, res) -> gds.listStatePlays(asPLong(req, P_GAME_ID)))
                .get("/:stateId", (req, res) -> gds.readStatePlay(asPLong(req, STATE_ID)))
                .delete("/:stateId", (req, res) ->
                        asResBool(gds.disableStatePlay(asPLong(req, STATE_ID))), UserRole.ADMIN);
            });
        });

    }


}
