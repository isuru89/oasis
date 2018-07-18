package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.LeaderboardDef;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.services.utils.RUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class GameDefService extends BaseService implements IGameDefService {

    private Long gameId;

    GameDefService(IOasisDao dao) {
        super(dao);
    }

    @Override
    public Long createGame(GameDef gameDef) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.GAME.getTypeId());
        wrapper.setName(gameDef.getName());
        wrapper.setDisplayName(gameDef.getDisplayName());
        wrapper.setContent(RUtils.toStr(gameDef, getMapper()));

        getDao().getDefinitionDao().addDefinition(wrapper);

        List<DefWrapper> wrappers = getDao().getDefinitionDao().listDefinitions(OasisDefinition.GAME.getTypeId());
        for (DefWrapper wrp : wrappers) {
            if (wrp.getName().equals(gameDef.getName())) {
                gameId = wrp.getGameId();
                break;
            }
        }
        return gameId;
    }

    @Override
    public GameDef readGame(long gameId) throws Exception {
        List<DefWrapper> wrappers = getDao().getDefinitionDao().listDefinitions(OasisDefinition.GAME.getTypeId());
        for (DefWrapper wrp : wrappers) {
            if (wrp.getId() == gameId) {
                GameDef gameDef = RUtils.toObj(wrp.getContent(), GameDef.class, getMapper());
                gameDef.setId(wrp.getId());
                return gameDef;
            }
        }
        throw new Exception("No game definition is found by id " + gameId + "!");
    }

    @Override
    public boolean addGameConstants(long gameId, Map<String, Object> gameConstants) throws Exception {
        List<DefWrapper> wrappers = getDao().getDefinitionDao().listDefinitions(OasisDefinition.GAME.getTypeId());
        for (DefWrapper wrp : wrappers) {
            if (wrp.getId() == gameId) {
                GameDef gameDef = RUtils.toObj(wrp.getContent(), GameDef.class, getMapper());
                gameDef.setConstants(gameConstants);
                wrp.setContent(RUtils.toStr(gameDef, getMapper()));

                getDao().getDefinitionDao().editDefinition(gameId, wrp);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeGameConstants(List<String> gameConstants) throws Exception {
        List<DefWrapper> wrappers = getDao().getDefinitionDao().listDefinitions(OasisDefinition.GAME.getTypeId());
        for (DefWrapper wrp : wrappers) {
            if (wrp.getId() == gameId) {
                GameDef gameDef = RUtils.toObj(wrp.getContent(), GameDef.class, getMapper());
                if (!RUtils.isNullOrEmpty(gameDef.getConstants())) {
                    for (String k : gameConstants) {
                        gameDef.getConstants().remove(k);
                    }
                }
                wrp.setContent(RUtils.toStr(gameDef, getMapper()));

                getDao().getDefinitionDao().editDefinition(gameId, wrp);
                return true;
            }
        }
        return false;
    }

    @Override
    public void addKpiCalculation(KpiDef fieldCalculator) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.KPI.getTypeId());
        wrapper.setName(fieldCalculator.getName());
        wrapper.setDisplayName(wrapper.getName());
        wrapper.setContent(RUtils.toStr(fieldCalculator, getMapper()));
        wrapper.setGameId(gameId);

        getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<KpiDef> listKpiCalculations() throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.KPI.getTypeId())
                .stream()
                .map(this::wrapperToKpi)
                .collect(Collectors.toList());
    }

    @Override
    public KpiDef readKpiCalculation(long id) throws Exception {
        return wrapperToKpi(getDao().getDefinitionDao().readDefinition(id));
    }

    @Override
    public boolean disableKpiCalculation(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
    }

    @Override
    public void addBadgeDef(BadgeDef badge) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.BADGE.getTypeId());
        wrapper.setName(badge.getName());
        wrapper.setDisplayName(badge.getDisplayName());
        wrapper.setContent(RUtils.toStr(badge, getMapper()));
        wrapper.setGameId(gameId);

        getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<BadgeDef> listBadgeDefs() throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.BADGE.getTypeId())
                .stream()
                .map(this::wrapperToBadge)
                .collect(Collectors.toList());
    }

    @Override
    public BadgeDef readBadgeDef(long id) throws Exception {
        return wrapperToBadge(getDao().getDefinitionDao().readDefinition(id));
    }

    @Override
    public boolean disableBadgeDef(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
    }

    @Override
    public List<PointDef> listPointDefs(long gameId) throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.POINT.getTypeId())
                .stream()
                .map(this::wrapperToPoint)
                .collect(Collectors.toList());
    }

    @Override
    public void addPointDef(PointDef pointRule) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.POINT.getTypeId());
        wrapper.setName(pointRule.getName());
        wrapper.setDisplayName(pointRule.getDisplayName());
        wrapper.setContent(RUtils.toStr(pointRule, getMapper()));
        wrapper.setGameId(gameId);

        getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public PointDef readPointDef(long id) throws Exception {
        return wrapperToPoint(getDao().getDefinitionDao().readDefinition(id));
    }

    @Override
    public boolean disablePointDef(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
    }

    @Override
    public void addMilestoneDef(MilestoneDef milestone) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.MILESTONE.getTypeId());
        wrapper.setName(milestone.getName());
        wrapper.setDisplayName(milestone.getDisplayName());
        wrapper.setContent(RUtils.toStr(milestone, getMapper()));
        wrapper.setGameId(gameId);

        getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<MilestoneDef> listMilestoneDefs() throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.MILESTONE.getTypeId())
                .stream()
                .map(this::wrapperToMilestone)
                .collect(Collectors.toList());
    }

    @Override
    public MilestoneDef readMilestoneDef(long id) throws Exception {
        return wrapperToMilestone(getDao().getDefinitionDao().readDefinition(id));
    }

    @Override
    public boolean disableMilestoneDef(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
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
    public boolean disableLeaderboardDef(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
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
    public boolean disableShopItem(long id) {
        return false;
    }

    private BadgeDef wrapperToBadge(DefWrapper wrapper) {
        BadgeDef badgeDef = RUtils.toObj(wrapper.getContent(), BadgeDef.class, getMapper());
        badgeDef.setId(wrapper.getId());
        badgeDef.setName(wrapper.getName());
        badgeDef.setDisplayName(wrapper.getDisplayName());
        return badgeDef;
    }

    private KpiDef wrapperToKpi(DefWrapper wrapper) {
        KpiDef kpiDef = RUtils.toObj(wrapper.getContent(), KpiDef.class, getMapper());
        kpiDef.setId(wrapper.getId());
        return kpiDef;
    }

    private PointDef wrapperToPoint(DefWrapper wrapper) {
        PointDef pointDef = RUtils.toObj(wrapper.getContent(), PointDef.class, getMapper());
        pointDef.setId(wrapper.getId());
        pointDef.setName(wrapper.getName());
        pointDef.setDisplayName(wrapper.getDisplayName());
        return pointDef;
    }

    private MilestoneDef wrapperToMilestone(DefWrapper wrapper) {
        MilestoneDef milestoneDef = RUtils.toObj(wrapper.getContent(), MilestoneDef.class, getMapper());
        milestoneDef.setId(wrapper.getId());
        milestoneDef.setName(wrapper.getName());
        milestoneDef.setDisplayName(wrapper.getDisplayName());
        return milestoneDef;
    }

}
