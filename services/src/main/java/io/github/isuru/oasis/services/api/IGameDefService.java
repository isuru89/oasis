package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.*;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;

import java.util.List;
import java.util.Map;

public interface IGameDefService {

    void createGame();
    void addGameConstants(Map<String, Object> gameConstants);
    void removeGameConstant(String constName);

    void addKpiCalculation(FieldCalculator fieldCalculator);
    List<FieldCalculator> listKipCalculations();
    FieldCalculator readKpiCalculation(long id);
    void disableKpiCalculation(long id);

    void addBadgeDef(BadgeRule badge);
    List<BadgeRule> listBadgeDefs();
    BadgeRule readBadgeDef(long id);
    void disableBadgeDef(long id);

    void addPointDef(PointRule pointRule);
    List<PointRule> listPointDefs();
    PointRule readPointDef(long id);
    void disablePointDef(long id);

    void addMilestoneDef(Milestone milestone);
    List<Milestone> listMilestoneDefs();
    Milestone readMilestoneDef(long id);
    void disableMilestoneDef(long id);

    void addLeaderboardDef(LeaderboardDef leaderboardDef);
    List<LeaderboardDef> listLeaderboardDefs();
    LeaderboardDef readLeaderboardDef();
    void disableLeaderboardDef(long id);

    void addShopItem(ShopItem item);
    List<ShopItem> listShopItems();
    ShopItem readShopItem(long id);
    void disableShopItem(long id);
}
