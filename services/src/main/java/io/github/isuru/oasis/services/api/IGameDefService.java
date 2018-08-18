package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.services.model.GameOptionsDto;

import java.util.List;
import java.util.Map;

public interface IGameDefService {

    Long createGame(GameDef gameDef, GameOptionsDto optionsDto) throws Exception;
    GameDef readGame(long gameId) throws Exception;
    List<GameDef> listGames() throws Exception;
    boolean disableGame(long gameId) throws Exception;
    boolean addGameConstants(long gameId, Map<String, Object> gameConstants) throws Exception;
    boolean removeGameConstants(long gameId, List<String> constName) throws Exception;

    long addKpiCalculation(long gameId, KpiDef fieldCalculator) throws Exception;
    List<KpiDef> listKpiCalculations() throws Exception;
    List<KpiDef> listKpiCalculations(long gameId) throws Exception;
    KpiDef readKpiCalculation(long id) throws Exception;
    boolean disableKpiCalculation(long id) throws Exception;

    long addBadgeDef(long gameId, BadgeDef badge) throws Exception;
    List<BadgeDef> listBadgeDefs() throws Exception;
    List<BadgeDef> listBadgeDefs(long gameId) throws Exception;
    BadgeDef readBadgeDef(long id) throws Exception;
    boolean disableBadgeDef(long id) throws Exception;

    long addPointDef(long gameId, PointDef pointRule) throws Exception;
    List<PointDef> listPointDefs(long gameId) throws Exception;
    PointDef readPointDef(long id) throws Exception;
    boolean disablePointDef(long id) throws Exception;

    long addMilestoneDef(long gameId, MilestoneDef milestone) throws Exception;
    List<MilestoneDef> listMilestoneDefs() throws Exception;
    List<MilestoneDef> listMilestoneDefs(long gameId) throws Exception;
    MilestoneDef readMilestoneDef(long id) throws Exception;
    boolean disableMilestoneDef(long id) throws Exception;

    long addLeaderboardDef(long gameId, LeaderboardDef leaderboardDef) throws Exception;
    List<LeaderboardDef> listLeaderboardDefs(long gameId) throws Exception;
    LeaderboardDef readLeaderboardDef(long id) throws Exception;
    boolean disableLeaderboardDef(long id) throws Exception;

    long addShopItem(long gameId, ShopItem item) throws Exception;
    List<ShopItem> listShopItems(long gameId) throws Exception;
    ShopItem readShopItem(long id) throws Exception;
    boolean disableShopItem(long id) throws Exception;

    long addChallenge(long gameId, ChallengeDef challengeDef) throws Exception;
    ChallengeDef readChallenge(long id) throws Exception;
    List<ChallengeDef> listChallenges(long gameId) throws Exception;
    boolean disableChallenge(long id) throws Exception;

    long addStatePlay(long gameId, StateDef stateDef) throws Exception;
    StateDef readStatePlay(long id) throws Exception;
    List<StateDef> listStatePlays(long gameId) throws Exception;
    boolean disableStatePlay(long id) throws Exception;
}
