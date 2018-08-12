package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.exception.OasisGameException;
import io.github.isuru.oasis.services.model.*;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.Maps;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author iweerarathna
 */
public class GameService extends BaseService implements IGameService {

    private String internalToken = null;

    GameService(IOasisDao oasisDao, IOasisApiService apiService) {
        super(oasisDao, apiService);
    }

    @Override
    public void awardPoints(long byUser, PointAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        Checks.greaterThanZero(awardDto.getToUser(), "toUser");
        Checks.validate(awardDto.getAmount() != 0.0f, "Point amount should not be equal to zero!");

        long teamId = getApiService().getProfileService().findCurrentTeamOfUser(awardDto.getToUser()).getTeamId();
        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_COMPENSATE_POINTS)
                .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                .put(Constants.FIELD_USER, awardDto.getToUser())
                .put(Constants.FIELD_TEAM, teamId)
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

        long teamId = getApiService().getProfileService().findCurrentTeamOfUser(awardDto.getToUser()).getTeamId();
        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_AWARD_BADGE)
                .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                .put(Constants.FIELD_USER, awardDto.getToUser())
                .put(Constants.FIELD_TEAM, teamId)
                .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                .put("badge", awardDto.getBadgeId())
                .put("subBadge", awardDto.getSubBadgeId())
                .put("tag", String.valueOf(byUser))
                .build();

        String token = getInternalToken();
        getApiService().getEventService().submitEvent(token, data);
    }

    @Override
    public void postAChallenge(ChallengeDef challengeDef, boolean startImmediate) throws Exception {
        //startDef(challengeDef.getId(), false);
    }

    @Override
    public void buyItem(long userBy, long itemId) throws Exception {
        ShopItem shopItem = getApiService().getGameDefService().readShopItem(itemId);
        if (shopItem != null) {
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
                    "profile/stats/getUserAvailablePoints",
                    Maps.create("userId", userBy));
            Map<String, Object> balanceMap = userPoints.iterator().next();
            float balance = ((Double) balanceMap.get("Balance")).floatValue();

            if (balance > price) {
                Map<String, Object> data = Maps.create()
                        .put("userId", userBy)
                        .put("itemId", itemId)
                        .put("cost", price)
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
            Map<String, Object> data = Maps.create().put("userId", userBy)
                    .put("itemId", itemId)
                    .put("currentEpoch", System.currentTimeMillis())
                    .put("amount", amount)
                    .build();
            long l = ctx.executeCommand("def/item/shareItem", data);
            if (l == amount) {
                Map<String, Object> item = Maps.create().put("userId", toUser)
                        .put("itemId", itemId)
                        .build();
                ctx.executeCommand("def/item/shareToItem", item);
                // @TODO add an event to stream processor
                return true;
            } else {
                throw new OasisGameException("Cannot share this item! Maybe the item itself is shared to you by friend!");
            }
        });
    }

    @Override
    public void readGameTimeline(long since) {

    }

    @Override
    public LeaderboardResponseDto readLeaderboardStatus(LeaderboardRequestDto request) throws Exception {
        Checks.validate(request.getType() != null, "Leaderboard type must not be null!");
        Checks.validate(request.getRangeStart() < request.getRangeEnd(),
                "Range end must be greater than its start value!");
        if (request.getForUser() != null && request.getForUser() > 0
                && (request.getTopN() != null || request.getBottomN() != null)) {
            throw new InputValidationException("Top or bottom listing is not supported when " +
                    "a specific user has been specified in the request!");
        }

        String scriptName = String.format("leaderboard/%s",
                request.getType().isCustom() ? "customRange" : "currentRange");

        List<LeaderboardRecordDto> recordDtos = toList(getDao().executeQuery(scriptName,
                Maps.create()
                    .put("startRange", request.getRangeStart())
                    .put("endRange", request.getRangeEnd())
                    .put("timePattern", request.getType().getPattern())
                    .put("topN", request.getTopN())
                    .put("bottomN", request.getBottomN())
                    .put("userId", request.getForUser())
                    .build(),
                LeaderboardRecordDto.class,
                Maps.create()
                    .put("special", request.getType().isCustom())
                    .put("teamWise", request.isTeamWise())
                    .put("teamScopeWise", request.isTeamScopeWise())
                    .put("topN", request.getTopN() != null && request.getTopN() > 0)
                    .put("bottomN", request.getBottomN() != null && request.getBottomN() > 0)
                    .put("hasUser", request.getForUser() != null && request.getForUser() > 0)
                    .build()));

        LeaderboardResponseDto responseDto = new LeaderboardResponseDto();
        responseDto.setRankings(recordDtos);
        responseDto.setRequest(request);
        return responseDto;
    }

    @Override
    public List<UserRankRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception {
        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasUser", isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .build();


        Maps.MapBuilder dataBuilder = Maps.create()
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd());

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

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasTeam", true)
                .put("hasUser", isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .build();

        TeamProfile teamProfile = getApiService().getProfileService().readTeam(teamId);

        Maps.MapBuilder dataBuilder = Maps.create()
                .put("teamId", teamId)
                .put("userId", request.getForUser())
                .put("teamScopeId", teamProfile.getTeamScope())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd());

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

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasTeam", false)
                .put("hasUser", isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Checks.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .build();

        Maps.MapBuilder dataBuilder = Maps.create()
                .put("teamScopeId", teamScopeId)
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return toList(getDao().executeQuery("leaderboard/teamLeaderboard",
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    private String getInternalToken() throws Exception {
        synchronized (this) {
            if (internalToken != null) {
                return internalToken;
            }
            Optional<EventSourceToken> eventSourceToken = getApiService().getEventService().readInternalSourceToken();
            eventSourceToken.ifPresent(eventSourceToken1 -> internalToken = eventSourceToken1.getToken());
            return internalToken;
        }
    }
}
