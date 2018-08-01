package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.AddGameDto;
import io.github.isuru.oasis.services.utils.Maps;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class DefinitionRouter extends BaseRouters {
    public DefinitionRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        post("/game", (req, res) -> {
            AddGameDto addGameDto = bodyAs(req, AddGameDto.class);
            return getGameDefService().createGame(addGameDto.getDef(), addGameDto.getOptions());
        });

        Spark.path("/game", () -> {
            Spark.get("/echo", (req, res) -> Maps.create("msg", "Welcome to Oasis!"), TRANSFORMER);

            get("/all", (req, res) -> {
                return getGameDefService().listGames();
            }).get("/:gameId", (req, res) -> {
                return getGameDefService().readGame(asPLong(req, "gameId"));
            }).delete("/:gameId", (req, res) -> {
                return getGameDefService().disableGame(asPLong(req, "gameId"));
            });

            post("/:gameId/kpi", (req, res) -> {
                return getGameDefService().addKpiCalculation(
                        asPLong(req, "gameId"),
                        bodyAs(req, KpiDef.class));
            });
            post("/:gameId/point", (req, res) -> {
                return getGameDefService().addPointDef(
                        asPLong(req, "gameId"),
                        bodyAs(req, PointDef.class));
            });
            post("/:gameId/badge", (req, res) -> {
                return getGameDefService().addBadgeDef(
                        asPLong(req, "gameId"),
                        bodyAs(req, BadgeDef.class));
            });
            post("/:gameId/milestone", (req, res) -> {
                return getGameDefService().addMilestoneDef(
                        asPLong(req, "gameId"),
                        bodyAs(req, MilestoneDef.class));
            });
            post("/:gameId/leaderboard", (req, res) -> {
                return getGameDefService().addLeaderboardDef(
                        asPLong(req, "gameId"),
                        bodyAs(req, LeaderboardDef.class));
            });
            post("/:gameId/challenge", (req, res) -> {
                return getGameDefService().addChallenge(
                        asPLong(req, "gameId"),
                        bodyAs(req, ChallengeDef.class));
            });
            post("/:gameId/item", (req, res) -> {
                return getGameDefService().addShopItem(
                        asPLong(req, "gameId"),
                        bodyAs(req, ShopItem.class));
            });

            Spark.path("/:gameId/kpi", () -> {
                get("/all", (req, res) -> getGameDefService().listKpiCalculations(asPLong(req, "gameId")))
                .get("/:kpiId", (req, res) -> getGameDefService().readKpiCalculation(asPLong(req, "kpiId")))
                .delete("/:kpiId", (req, res) -> getGameDefService().disableKpiCalculation(asPLong(req, "kpiId")));
            });

            Spark.path("/:gameId/point", () -> {
                get("/all", (req, res) -> getGameDefService().listPointDefs(asPLong(req, "gameId")))
                .get("/:pointId", (req, res) -> getGameDefService().readPointDef(asPLong(req, "pointId")))
                .delete("/:pointId", (req, res) -> getGameDefService().disablePointDef(asPLong(req, "pointId")));
            });

            Spark.path("/:gameId/badge", () -> {
                get("/all", (req, res) -> getGameDefService().listBadgeDefs(asPLong(req, "gameId")))
                .get("/:badgeId", (req, res) -> getGameDefService().readBadgeDef(asPLong(req, "badgeId")))
                .delete("/:badgeId", (req, res) -> getGameDefService().disableBadgeDef(asPLong(req, "badgeId")));
            });

            Spark.path("/:gameId/milestone", () -> {
                get("/all", (req, res) -> getGameDefService().listMilestoneDefs(asPLong(req, "gameId")))
                .get("/:mid", (req, res) -> getGameDefService().readMilestoneDef(asPLong(req, "mid")))
                .delete("/:mid", (req, res) -> getGameDefService().disableMilestoneDef(asPLong(req, "mid")));
            });

            Spark.path("/:gameId/leaderboard", () -> {
                get("/all", (req, res) -> getGameDefService().listLeaderboardDefs(asPLong(req, "gameId")))
                .get("/:lid", (req, res) -> getGameDefService().readLeaderboardDef(asPLong(req, "lid")))
                .delete("/:lid", (req, res) -> getGameDefService().disableLeaderboardDef(asPLong(req, "lid")));
            });

            Spark.path("/:gameId/challenge", () -> {
                get("/all", (req, res) -> getGameDefService().listChallenges(asPLong(req, "gameId")))
                .get("/:cid", (req, res) -> getGameDefService().readChallenge(asPLong(req, "cid")))
                .delete("/:cid", (req, res) -> getGameDefService().disableChallenge(asPLong(req, "cid")));
            });

            Spark.path("/:gameId/item", () -> {
                get("/all", (req, res) -> getGameDefService().listShopItems(asPLong(req, "gameId")))
                .get("/:iid", (req, res) -> getGameDefService().readShopItem(asPLong(req, "iid")))
                .delete("/:iid", (req, res) -> getGameDefService().disableShopItem(asPLong(req, "iid")));
            });
        });

    }


}
