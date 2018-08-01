package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.Converters;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.GameOptionsDto;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.RUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class GameDefService extends BaseService implements IGameDefService {

    GameDefService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);
    }

    @Override
    public Long createGame(GameDef gameDef, GameOptionsDto optionsDto) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.GAME.getTypeId());
        wrapper.setName(gameDef.getName());
        wrapper.setDisplayName(gameDef.getDisplayName());
        wrapper.setContent(RUtils.toStr(gameDef, getMapper()));

        getDao().getDefinitionDao().addDefinition(wrapper);

        long gameId = -1;
        List<DefWrapper> wrappers = getDao().getDefinitionDao().listDefinitions(OasisDefinition.GAME.getTypeId());
        for (DefWrapper wrp : wrappers) {
            if (wrp.getName().equals(gameDef.getName())) {
                gameId = wrp.getId();
                break;
            }
        }

        if (gameId < 0) {
            throw new Exception("Game add failed!");
        }

        if (optionsDto.isAllowPointCompensation()) {
            // add compensation point event
            PointDef compDef = new PointDef();
            compDef.setName(EventNames.POINT_RULE_COMPENSATION_NAME);
            compDef.setDisplayName("Rule to compensate points at any time.");
            compDef.setAmount("amount");
            compDef.setEvent(EventNames.EVENT_COMPENSATE_POINTS);
            compDef.setCondition("true");
            addPointDef(gameId, compDef);
        }

        if (optionsDto.isAwardPointsForMilestoneCompletion()) {
            PointDef msCompleteDef = new PointDef();
            msCompleteDef.setName(EventNames.POINT_RULE_MILESTONE_BONUS_NAME);
            msCompleteDef.setDisplayName("Award points when certain milestones are completed.");
            msCompleteDef.setAmount(optionsDto.getDefaultBonusPointsForMilestone());
            addPointDef(gameId, msCompleteDef);
        }

        if (optionsDto.isAwardPointsForBadges()) {
            PointDef bdgCompleteDef = new PointDef();
            bdgCompleteDef.setName(EventNames.POINT_RULE_BADGE_BONUS_NAME);
            bdgCompleteDef.setDisplayName("Award points when certain badges are completed.");
            bdgCompleteDef.setAmount(optionsDto.getDefaultBonusPointsForBadge());
            addPointDef(gameId, bdgCompleteDef);
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
    public List<GameDef> listGames() throws Exception {
        List<DefWrapper> wrappers = getDao().getDefinitionDao().listDefinitions(OasisDefinition.GAME.getTypeId());
        List<GameDef> gameDefs = new LinkedList<>();
        for (DefWrapper wrp : wrappers) {
            GameDef gameDef = RUtils.toObj(wrp.getContent(), GameDef.class, getMapper());
            gameDef.setId(wrp.getId());
            gameDefs.add(gameDef);
        }
        return gameDefs;
    }

    @Override
    public boolean disableGame(long gameId) throws Exception {
        getDao().executeCommand("def/disableAllGameDefs", Maps.create("gameId", gameId));
        return getDao().getDefinitionDao().disableDefinition(gameId);
    }

    @Override
    public boolean addGameConstants(long gameId, Map<String, Object> gameConstants) throws Exception {
        DefWrapper wrp = getDao().getDefinitionDao().readDefinition(gameId);

        GameDef gameDef = RUtils.toObj(wrp.getContent(), GameDef.class, getMapper());
        gameDef.setConstants(gameConstants);
        wrp.setContent(RUtils.toStr(gameDef, getMapper()));

        return getDao().getDefinitionDao().editDefinition(wrp.getId(), wrp) > 0;
    }

    @Override
    public boolean removeGameConstants(long gameId, List<String> gameConstants) throws Exception {
        DefWrapper wrp = getDao().getDefinitionDao().readDefinition(gameId);

        GameDef gameDef = RUtils.toObj(wrp.getContent(), GameDef.class, getMapper());
        if (!RUtils.isNullOrEmpty(gameDef.getConstants())) {
            for (String k : gameConstants) {
                gameDef.getConstants().remove(k);
            }
        }
        wrp.setContent(RUtils.toStr(gameDef, getMapper()));

        return getDao().getDefinitionDao().editDefinition(wrp.getId(), wrp) > 0;
    }

    @Override
    public long addKpiCalculation(long gameId, KpiDef fieldCalculator) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.KPI.getTypeId());
        wrapper.setName(fieldCalculator.getName());
        wrapper.setDisplayName(wrapper.getName());
        wrapper.setContent(RUtils.toStr(fieldCalculator, getMapper()));
        wrapper.setGameId(gameId);

        return getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<KpiDef> listKpiCalculations() throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.KPI.getTypeId())
                .stream()
                .map(this::wrapperToKpi)
                .collect(Collectors.toList());
    }

    @Override
    public List<KpiDef> listKpiCalculations(long gameId) throws Exception {
        return getDao().getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.KPI.getTypeId())
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
    public long addBadgeDef(long gameId, BadgeDef badge) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.BADGE.getTypeId());
        wrapper.setName(badge.getName());
        wrapper.setDisplayName(badge.getDisplayName());
        wrapper.setContent(RUtils.toStr(badge, getMapper()));
        wrapper.setGameId(gameId);

        return getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<BadgeDef> listBadgeDefs() throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.BADGE.getTypeId())
                .stream()
                .map(this::wrapperToBadge)
                .collect(Collectors.toList());
    }

    @Override
    public List<BadgeDef> listBadgeDefs(long gameId) throws Exception {
        return getDao().getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.BADGE.getTypeId())
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
        return getDao().getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.POINT.getTypeId())
                .stream()
                .map(this::wrapperToPoint)
                .collect(Collectors.toList());
    }

    @Override
    public long addPointDef(long gameId, PointDef pointRule) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.POINT.getTypeId());
        wrapper.setName(pointRule.getName());
        wrapper.setDisplayName(pointRule.getDisplayName());
        wrapper.setContent(RUtils.toStr(pointRule, getMapper()));
        wrapper.setGameId(gameId);

        return getDao().getDefinitionDao().addDefinition(wrapper);
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
    public long addMilestoneDef(long gameId, MilestoneDef milestone) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.MILESTONE.getTypeId());
        wrapper.setName(milestone.getName());
        wrapper.setDisplayName(milestone.getDisplayName());
        wrapper.setContent(RUtils.toStr(milestone, getMapper()));
        wrapper.setGameId(gameId);

        return getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<MilestoneDef> listMilestoneDefs() throws Exception {
        return getDao().getDefinitionDao().listDefinitions(OasisDefinition.MILESTONE.getTypeId())
                .stream()
                .map(this::wrapperToMilestone)
                .collect(Collectors.toList());
    }

    @Override
    public List<MilestoneDef> listMilestoneDefs(long gameId) throws Exception {
        return getDao().getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.MILESTONE.getTypeId())
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
    public long addLeaderboardDef(long gameId, LeaderboardDef leaderboardDef) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.LEADERBOARD.getTypeId());
        wrapper.setName(leaderboardDef.getName());
        wrapper.setDisplayName(leaderboardDef.getDisplayName());
        wrapper.setContent(RUtils.toStr(leaderboardDef, getMapper()));
        wrapper.setGameId(gameId);

        return getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public List<LeaderboardDef> listLeaderboardDefs(long gameId) throws Exception {
        return getDao().getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.LEADERBOARD.getTypeId())
                .stream()
                .map(this::wrapperToLeaderboard)
                .collect(Collectors.toList());
    }

    @Override
    public LeaderboardDef readLeaderboardDef(long id) throws Exception {
        return wrapperToLeaderboard(getDao().getDefinitionDao().readDefinition(id));
    }

    @Override
    public boolean disableLeaderboardDef(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
    }

    @Override
    public long addShopItem(long gameId, ShopItem item) throws Exception {
        Map<String, Object> data = Maps.create()
                .put("title", item.getTitle())
                .put("description", item.getDescription())
                .put("scope", item.getScope())
                .put("level", item.getLevel())
                .put("price", item.getPrice())
                .put("imageRef", item.getImageRef())
                .put("expirationAt", item.getExpirationAt())
                .build();

        return getDao().executeInsert("def/item/addShopItem", data, "id");
    }

    @Override
    public List<ShopItem> listShopItems(long gameId) throws Exception {
        Iterable<ShopItem> items = getDao().executeQuery("def/item/listItems", null, ShopItem.class);
        LinkedList<ShopItem> shopItems = new LinkedList<>();
        for (ShopItem item : items) {
            shopItems.add(item);
        }
        return shopItems;
    }

    @Override
    public ShopItem readShopItem(long id) throws Exception {
        return getTheOnlyRecord("def/item/readItem",
                Maps.create("itemId", id),
                ShopItem.class);
    }

    @Override
    public long addChallenge(long gameId, ChallengeDef challengeDef) throws Exception {
        DefWrapper wrapper = new DefWrapper();
        wrapper.setKind(OasisDefinition.CHALLENGE.getTypeId());
        wrapper.setName(challengeDef.getName());
        wrapper.setDisplayName(challengeDef.getDisplayName());
        wrapper.setContent(RUtils.toStr(challengeDef, getMapper()));
        wrapper.setGameId(gameId);

        return getDao().getDefinitionDao().addDefinition(wrapper);
    }

    @Override
    public ChallengeDef readChallenge(long id) throws Exception {
        return wrapperToChallenge(getDao().getDefinitionDao().readDefinition(id));
    }

    @Override
    public List<ChallengeDef> listChallenges(long gameId) throws Exception {
        return getDao().getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.CHALLENGE.getTypeId())
                .stream()
                .map(this::wrapperToChallenge)
                .collect(Collectors.toList());
    }

    @Override
    public boolean disableChallenge(long id) throws Exception {
        return getDao().getDefinitionDao().disableDefinition(id);
    }

    @Override
    public boolean disableShopItem(long id) throws Exception {
        return getDao().executeCommand("def/item/disableItem",
                Maps.create("itemId", id)) > 0;
    }

    private ChallengeDef wrapperToChallenge(DefWrapper wrapper) {
        return Converters.toChallengeDef(wrapper,
                wrp -> RUtils.toObj(wrp.getContent(), ChallengeDef.class, getMapper()));
    }

    private BadgeDef wrapperToBadge(DefWrapper wrapper) {
        return Converters.toBadgeDef(wrapper,
                wrp -> RUtils.toObj(wrp.getContent(), BadgeDef.class, getMapper()));
    }

    private KpiDef wrapperToKpi(DefWrapper wrapper) {
        return Converters.toKpiDef(wrapper,
                wrp -> RUtils.toObj(wrp.getContent(), KpiDef.class, getMapper()));
    }

    private PointDef wrapperToPoint(DefWrapper wrapper) {
        return Converters.toPointDef(wrapper,
                wrp -> RUtils.toObj(wrp.getContent(), PointDef.class, getMapper()));
    }

    private MilestoneDef wrapperToMilestone(DefWrapper wrapper) {
        return Converters.toMilestoneDef(wrapper,
                wrp -> RUtils.toObj(wrp.getContent(), MilestoneDef.class, getMapper()));
    }

    private LeaderboardDef wrapperToLeaderboard(DefWrapper wrapper) {
        return Converters.toLeaderboardDef(wrapper,
                wrp -> RUtils.toObj(wrp.getContent(), LeaderboardDef.class, getMapper()));
    }

}
