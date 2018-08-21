package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.exception.OasisGameException;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.PointAwardDto;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserRankRecordDto;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class GameService extends BaseService implements IGameService {

    GameService(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void awardPoints(long byUser, PointAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        Checks.greaterThanZero(awardDto.getToUser(), "toUser");
        Checks.validate(awardDto.getAmount() != 0.0f, "Point amount should not be equal to zero!");

        UserTeam currentTeamOfUser = getApiService().getProfileService().findCurrentTeamOfUser(awardDto.getToUser());
        long teamId = currentTeamOfUser != null ? currentTeamOfUser.getTeamId() : DataCache.get().getTeamDefault().getId();
        long scopeId = currentTeamOfUser != null ? currentTeamOfUser.getScopeId() : DataCache.get().getTeamScopeDefault().getId();
        long gameId = awardDto.getGameId() != null ? awardDto.getGameId() : DataCache.get().getDefGameId();

        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_COMPENSATE_POINTS)
                .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                .put(Constants.FIELD_USER, awardDto.getToUser())
                .put(Constants.FIELD_TEAM, teamId)
                .put(Constants.FIELD_SCOPE, scopeId)
                .put(Constants.FIELD_GAME_ID, gameId)
                .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                .put("amount", awardDto.getAmount())
                .put("tag", String.valueOf(byUser))
                .build();

        String token = getInternalToken();
        getApiService().getEventService().submitEvent(token, data);
    }

    @Override
    public void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        Checks.greaterThanZero(awardDto.getToUser(), "toUser");
        Checks.greaterThanZero(awardDto.getBadgeId(), "Badge id must be a valid one!");

        UserTeam currentTeamOfUser = getApiService().getProfileService().findCurrentTeamOfUser(awardDto.getToUser());
        long teamId = currentTeamOfUser != null ? currentTeamOfUser.getTeamId() : DataCache.get().getTeamDefault().getId();
        long scopeId = currentTeamOfUser != null ? currentTeamOfUser.getScopeId() : DataCache.get().getTeamScopeDefault().getId();
        long gameId = awardDto.getGameId() != null ? awardDto.getGameId() : DataCache.get().getDefGameId();

        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_AWARD_BADGE)
                .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                .put(Constants.FIELD_USER, awardDto.getToUser())
                .put(Constants.FIELD_TEAM, teamId)
                .put(Constants.FIELD_SCOPE, scopeId)
                .put(Constants.FIELD_GAME_ID, gameId)
                .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                .put("badge", awardDto.getBadgeId())
                .put("subBadge", awardDto.getSubBadgeId())
                .put("tag", String.valueOf(byUser))
                .build();

        String token = getInternalToken();
        getApiService().getEventService().submitEvent(token, data);
    }

    @Override
    public void buyItem(long userBy, long itemId) throws Exception {
        ShopItem shopItem = getApiService().getGameDefService().readShopItem(itemId);
        if (shopItem != null) {
            if (shopItem.getExpirationAt() != null && shopItem.getExpirationAt() < System.currentTimeMillis()) {
                // if item is expired, then disable it.
                getApiService().getGameDefService().disableShopItem(itemId);
                throw new InputValidationException("Item '" + itemId + "' is already expired!");
            }

            buyItem(userBy, itemId, shopItem.getPrice());
        } else {
            throw new InputValidationException("No item is found by id " + itemId + "!");
        }
    }

    @Override
    public void buyItem(long userBy, long itemId, float price) throws Exception {
        Checks.greaterThanZero(userBy, "userId");
        Checks.greaterThanZero(itemId, "itemId");

        getDao().runTx(Connection.TRANSACTION_READ_COMMITTED, ctx -> {
            Iterable<Map<String, Object>> userPoints = ctx.executeQuery(
                    "stats/getUserAvailablePoints",
                    Maps.create("userId", userBy));
            Map<String, Object> balanceMap = userPoints.iterator().next();
            float balance = ((Double) balanceMap.get("Balance")).floatValue();

            if (balance > price) {
                Iterable<UserTeam> userTeams = ctx.executeQuery("profile/findCurrentTeamOfUser",
                        Maps.create()
                                .put("userId", userBy)
                                .put("currentEpoch", System.currentTimeMillis()).build(),
                        UserTeam.class);
                UserTeam currTeam = null;
                if (userTeams.iterator().hasNext()) {
                    currTeam = userTeams.iterator().next();
                }

                long teamId = currTeam != null ? currTeam.getTeamId() : DataCache.get().getTeamDefault().getId();
                long scopeId = currTeam != null ? currTeam.getScopeId() : DataCache.get().getTeamScopeDefault().getId();

                Map<String, Object> data = Maps.create()
                        .put("userId", userBy)
                        .put("itemId", itemId)
                        .put("cost", price)
                        .put("teamId", teamId)
                        .put("scopeId", scopeId)
                        .put("purchasedAt", System.currentTimeMillis())
                        .build();
                ctx.executeCommand("def/item/buyItem", data);
                return true;
            } else {
                throw new OasisGameException("You do not have enough money to buy this item!");
            }
        });
    }

    @Override
    public void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception {
        Checks.greaterThanZero(userBy, "userId");
        Checks.greaterThanZero(itemId, "itemId");
        Checks.greaterThanZero(toUser, "toUser");

        getDao().runTx(Connection.TRANSACTION_READ_COMMITTED, ctx -> {
            long currTs = System.currentTimeMillis();
            Map<String, Object> data = Maps.create().put("userId", userBy)
                    .put("itemId", itemId)
                    .put("currentEpoch", currTs)
                    .put("amount", amount)
                    .build();
            long l = ctx.executeCommand("def/item/shareItem", data);
            if (l == amount) {
                Iterable<UserTeam> userTeams = ctx.executeQuery("profile/findCurrentTeamOfUser",
                        Maps.create().put("userId", toUser)
                                .put("currentEpoch", currTs).build(),
                        UserTeam.class);
                UserTeam currTeam = null;
                if (userTeams.iterator().hasNext()) {
                    currTeam = userTeams.iterator().next();
                }

                long teamId = currTeam != null ? currTeam.getTeamId() : DataCache.get().getTeamDefault().getId();
                long scopeId = currTeam != null ? currTeam.getScopeId() : DataCache.get().getTeamScopeDefault().getId();

                Map<String, Object> item = Maps.create().put("userId", toUser)
                        .put("teamId", teamId).put("teamScopeId", scopeId)
                        .put("itemId", itemId)
                        .build();
                ctx.executeCommand("def/item/shareToItem", item);

                // add an event to stream processor
                getApiService().getEventService().submitEvent(
                        DataCache.get().getInternalEventSourceToken().getToken(),
                        Maps.create()
                        .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_SHOP_ITEM_SHARE)
                        .put(Constants.FIELD_TIMESTAMP, currTs)
                        .put(Constants.FIELD_USER, userBy)
                        .put(Constants.FIELD_TEAM, teamId)
                        .put(Constants.FIELD_SCOPE, scopeId)
                        .put(Constants.FIELD_GAME_ID, DataCache.get().getDefGameId())
                        .put(Constants.FIELD_ID, null)
                        .put("toUser", toUser)
                        .put("itemId", itemId)
                        .put("itemAmount", amount)
                        .build());


                return true;
            } else {
                throw new OasisGameException("Cannot share this item! Maybe the item itself is shared to you by friend!");
            }
        });
    }

    @Override
    public List<UserRankRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception {
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasUser", isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", isValid(request.getTopN()))
                .put("isBottomN", isValid(request.getBottomN()))
                .build();


        Maps.MapBuilder dataBuilder = Maps.create()
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("bottomN", request.getBottomN());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return toList(getDao().executeQuery("leaderboard/globalLeaderboard",
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    @Override
    public List<UserRankRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasTeam", true)
                .put("hasUser", isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", isValid(request.getTopN()))
                .put("isBottomN", isValid(request.getBottomN()))
                .build();

        TeamProfile teamProfile = getApiService().getProfileService().readTeam(teamId);

        Maps.MapBuilder dataBuilder = Maps.create()
                .put("teamId", teamId)
                .put("userId", request.getForUser())
                .put("teamScopeId", teamProfile.getTeamScope())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("bottomN", request.getBottomN());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return toList(getDao().executeQuery("leaderboard/teamLeaderboard",
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    @Override
    public List<UserRankRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception {
        Checks.greaterThanZero(teamScopeId, "teamScopeId");
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasTeam", false)
                .put("hasUser", isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", isValid(request.getTopN()))
                .put("isBottomN", isValid(request.getBottomN()))
                .build();

        Maps.MapBuilder dataBuilder = Maps.create()
                .put("teamScopeId", teamScopeId)
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("bottomN", request.getBottomN());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return toList(getDao().executeQuery("leaderboard/teamLeaderboard",
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    private String getInternalToken() {
        return DataCache.get().getInternalEventSourceToken().getToken();
    }

    private void checkLeaderboardRequest(LeaderboardRequestDto dto) throws InputValidationException {
        Checks.validate(dto.getRangeStart() <= dto.getRangeEnd(), "Time range end must be greater than or equal to start time!");
        if (isValid(dto.getForUser()) && (isValid(dto.getTopN()) || isValid(dto.getBottomN()))) {
            throw new InputValidationException("Top or bottom listing is not supported when " +
                    "a specific user has been specified in the request!");
        }
    }
}
