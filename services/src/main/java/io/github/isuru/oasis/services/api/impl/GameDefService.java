package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.LeaderboardDef;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class GameDefService implements IGameDefService {
    @Override
    public void createGame() {

    }

    @Override
    public void addGameConstants(Map<String, Object> gameConstants) {

    }

    @Override
    public void removeGameConstant(String constName) {

    }

    @Override
    public void addKpiCalculation(FieldCalculator fieldCalculator) {

    }

    @Override
    public List<FieldCalculator> listKipCalculations() {
        return null;
    }

    @Override
    public FieldCalculator readKpiCalculation(long id) {
        return null;
    }

    @Override
    public void disableKpiCalculation(long id) {

    }

    @Override
    public void addBadgeDef(BadgeRule badge) {

    }

    @Override
    public List<BadgeRule> listBadgeDefs() {
        return null;
    }

    @Override
    public BadgeRule readBadgeDef(long id) {
        return null;
    }

    @Override
    public void disableBadgeDef(long id) {

    }

    @Override
    public void addPointDef(PointRule pointRule) {

    }

    @Override
    public List<PointRule> listPointDefs() {
        return null;
    }

    @Override
    public PointRule readPointDef(long id) {
        return null;
    }

    @Override
    public void disablePointDef(long id) {

    }

    @Override
    public void addMilestoneDef(Milestone milestone) {

    }

    @Override
    public List<Milestone> listMilestoneDefs() {
        return null;
    }

    @Override
    public Milestone readMilestoneDef(long id) {
        return null;
    }

    @Override
    public void disableMilestoneDef(long id) {

    }

    @Override
    public void addLeaderboardDef(LeaderboardDef leaderboardDef) {

    }

    @Override
    public List<LeaderboardDef> listLeaderboardDefs() {
        return null;
    }

    @Override
    public LeaderboardDef readLeaderboardDef() {
        return null;
    }

    @Override
    public void disableLeaderboardDef(long id) {

    }

    @Override
    public void addShopItem(ShopItem item) {

    }

    @Override
    public List<ShopItem> listShopItems() {
        return null;
    }

    @Override
    public ShopItem readShopItem(long id) {
        return null;
    }

    @Override
    public void disableShopItem(long id) {

    }
}
