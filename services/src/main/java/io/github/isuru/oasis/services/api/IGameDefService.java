package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.LeaderboardDef;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;

import java.util.List;
import java.util.Map;

public interface IGameDefService {

    void createGame();
    void addGameConstants(Map<String, Object> gameConstants);
    boolean removeGameConstant(String constName);

    void addKpiCalculation(KpiDef fieldCalculator) throws Exception;
    List<KpiDef> listKpiCalculations() throws Exception;
    KpiDef readKpiCalculation(long id) throws Exception;
    boolean disableKpiCalculation(long id) throws Exception;

    void addBadgeDef(BadgeDef badge) throws Exception;
    List<BadgeDef> listBadgeDefs() throws Exception;
    BadgeDef readBadgeDef(long id) throws Exception;
    boolean disableBadgeDef(long id) throws Exception;

    void addPointDef(PointDef pointRule) throws Exception;
    List<PointDef> listPointDefs(long gameId) throws Exception;
    PointDef readPointDef(long id) throws Exception;
    boolean disablePointDef(long id) throws Exception;

    void addMilestoneDef(MilestoneDef milestone) throws Exception;
    List<MilestoneDef> listMilestoneDefs() throws Exception;
    MilestoneDef readMilestoneDef(long id) throws Exception;
    boolean disableMilestoneDef(long id) throws Exception;

    void addLeaderboardDef(LeaderboardDef leaderboardDef) throws Exception;
    List<LeaderboardDef> listLeaderboardDefs() throws Exception;
    LeaderboardDef readLeaderboardDef() throws Exception;
    boolean disableLeaderboardDef(long id) throws Exception;

    void addShopItem(ShopItem item) throws Exception;
    List<ShopItem> listShopItems() throws Exception;
    ShopItem readShopItem(long id) throws Exception;
    boolean disableShopItem(long id) throws Exception;
}
