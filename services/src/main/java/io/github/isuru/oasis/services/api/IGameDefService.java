package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;

import java.util.List;
import java.util.Map;

public interface IGameDefService {

    Long createGame(GameDef gameDef) throws Exception;
    GameDef readGame(long gameId) throws Exception;
    List<GameDef> listGames() throws Exception;
    boolean disableGame(long gameId) throws Exception;
    boolean addGameConstants(long gameId, Map<String, Object> gameConstants) throws Exception;
    boolean removeGameConstants(long gameId, List<String> constName) throws Exception;

    long addKpiCalculation(KpiDef fieldCalculator) throws Exception;
    List<KpiDef> listKpiCalculations() throws Exception;
    List<KpiDef> listKpiCalculations(long gameId) throws Exception;
    KpiDef readKpiCalculation(long id) throws Exception;
    boolean disableKpiCalculation(long id) throws Exception;

    long addBadgeDef(BadgeDef badge) throws Exception;
    List<BadgeDef> listBadgeDefs() throws Exception;
    List<BadgeDef> listBadgeDefs(long gameId) throws Exception;
    BadgeDef readBadgeDef(long id) throws Exception;
    boolean disableBadgeDef(long id) throws Exception;

    long addPointDef(PointDef pointRule) throws Exception;
    List<PointDef> listPointDefs(long gameId) throws Exception;
    PointDef readPointDef(long id) throws Exception;
    boolean disablePointDef(long id) throws Exception;

    long addMilestoneDef(MilestoneDef milestone) throws Exception;
    List<MilestoneDef> listMilestoneDefs() throws Exception;
    List<MilestoneDef> listMilestoneDefs(long gameId) throws Exception;
    MilestoneDef readMilestoneDef(long id) throws Exception;
    boolean disableMilestoneDef(long id) throws Exception;

    long addLeaderboardDef(LeaderboardDef leaderboardDef) throws Exception;
    List<LeaderboardDef> listLeaderboardDefs() throws Exception;
    LeaderboardDef readLeaderboardDef(long id) throws Exception;
    boolean disableLeaderboardDef(long id) throws Exception;

    void addShopItem(ShopItem item) throws Exception;
    List<ShopItem> listShopItems() throws Exception;
    ShopItem readShopItem(long id) throws Exception;
    boolean disableShopItem(long id) throws Exception;
}
