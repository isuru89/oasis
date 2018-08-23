package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.AddGameDto;
import io.github.isuru.oasis.services.utils.UserRole;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class DefinitionRouter extends BaseRouters {

    private static final String P_GAME_ID = "gameId";

    DefinitionRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        IGameDefService gds = getApiService().getGameDefService();

        post("/game", (req, res) -> {
            AddGameDto addGameDto = bodyAs(req, AddGameDto.class);
            return asResAdd(gds.createGame(addGameDto.getDef(), addGameDto.getOptions()));
        }, UserRole.ADMIN);

        Spark.path("/game", () -> {
            get("/all", (req, res) -> gds.listGames())
            .get("/:gameId", (req, res) -> gds.readGame(asPLong(req, P_GAME_ID)))
            .delete("/:gameId",
                    (req, res) -> asResBool(gds.disableGame(asPLong(req, P_GAME_ID))), UserRole.ADMIN);

            post("/:gameId/kpi", (req, res) -> asResAdd(gds.addKpiCalculation(asPLong(req, P_GAME_ID), bodyAs(req, KpiDef.class))), UserRole.ADMIN);
            post("/:gameId/point", (req, res) -> asResAdd(gds.addPointDef(asPLong(req, P_GAME_ID), bodyAs(req, PointDef.class))), UserRole.ADMIN);
            post("/:gameId/badge", (req, res) -> asResAdd(gds.addBadgeDef(asPLong(req, P_GAME_ID), bodyAs(req, BadgeDef.class))), UserRole.ADMIN);
            post("/:gameId/milestone", (req, res) -> asResAdd(gds.addMilestoneDef(asPLong(req, P_GAME_ID), bodyAs(req, MilestoneDef.class))), UserRole.ADMIN);
            post("/:gameId/leaderboard", (req, res) -> asResAdd(gds.addLeaderboardDef(asPLong(req, P_GAME_ID), bodyAs(req, LeaderboardDef.class))), UserRole.CURATOR);
            post("/:gameId/challenge", (req, res) -> asResAdd(gds.addChallenge(asPLong(req, P_GAME_ID), bodyAs(req, ChallengeDef.class))), UserRole.ADMIN);
            post("/:gameId/item", (req, res) -> asResAdd(gds.addShopItem(asPLong(req, P_GAME_ID), bodyAs(req, ShopItem.class))), UserRole.ADMIN);
            post("/:gameId/state", (req, res) -> asResAdd(gds.addStatePlay(asPLong(req, P_GAME_ID), bodyAs(req, StateDef.class))), UserRole.ADMIN);

            Spark.path("/:gameId/kpi", () -> {
                get("/all", (req, res) -> gds.listKpiCalculations(asPLong(req, P_GAME_ID)))
                .get("/:kpiId", (req, res) -> gds.readKpiCalculation(asPLong(req, "kpiId")))
                .delete("/:kpiId", (req, res) ->
                        asResBool(gds.disableKpiCalculation(asPLong(req, "kpiId"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/point", () -> {
                get("/all", (req, res) -> gds.listPointDefs(asPLong(req, P_GAME_ID)))
                .get("/:pointId", (req, res) -> gds.readPointDef(asPLong(req, "pointId")))
                .delete("/:pointId", (req, res) ->
                        asResBool(gds.disablePointDef(asPLong(req, "pointId"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/badge", () -> {
                get("/all", (req, res) -> gds.listBadgeDefs(asPLong(req, P_GAME_ID)))
                .get("/:badgeId", (req, res) -> gds.readBadgeDef(asPLong(req, "badgeId")))
                .delete("/:badgeId", (req, res) ->
                        asResBool(gds.disableBadgeDef(asPLong(req, "badgeId"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/milestone", () -> {
                get("/all", (req, res) -> gds.listMilestoneDefs(asPLong(req, P_GAME_ID)))
                .get("/:mid", (req, res) -> gds.readMilestoneDef(asPLong(req, "mid")))
                .delete("/:mid", (req, res) ->
                        asResBool(gds.disableMilestoneDef(asPLong(req, "mid"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/leaderboard", () -> {
                get("/all", (req, res) -> gds.listLeaderboardDefs(asPLong(req, P_GAME_ID)))
                .get("/:lid", (req, res) -> gds.readLeaderboardDef(asPLong(req, "lid")))
                .delete("/:lid", (req, res) ->
                        asResBool(gds.disableLeaderboardDef(asPLong(req, "lid"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/challenge", () -> {
                get("/all", (req, res) -> gds.listChallenges(asPLong(req, P_GAME_ID)))
                .get("/:cid", (req, res) -> gds.readChallenge(asPLong(req, "cid")))
                .delete("/:cid", (req, res) ->
                        asResBool(gds.disableChallenge(asPLong(req, "cid"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/item", () -> {
                get("/all", (req, res) -> gds.listShopItems(asPLong(req, P_GAME_ID)))
                .get("/:iid", (req, res) -> gds.readShopItem(asPLong(req, "iid")))
                .delete("/:iid", (req, res) ->
                        asResBool(gds.disableShopItem(asPLong(req, "iid"))), UserRole.ADMIN);
            });

            Spark.path("/:gameId/state", () -> {
                get("/all", (req, res) -> gds.listStatePlays(asPLong(req, P_GAME_ID)))
                .get("/:sid", (req, res) -> gds.readStatePlay(asPLong(req, "sid")))
                .delete("/:sid", (req, res) ->
                        asResBool(gds.disableStatePlay(asPLong(req, "sid"))), UserRole.ADMIN);
            });
        });

    }


}
