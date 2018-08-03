package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.LeaderboardRecordDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.LeaderboardResponseDto;
import io.github.isuru.oasis.services.model.PointAwardDto;
import io.github.isuru.oasis.services.utils.Maps;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class GameService extends BaseService implements IGameService {

    GameService(IOasisDao oasisDao, IOasisApiService apiService) {
        super(oasisDao, apiService);
    }

    @Override
    public void awardPoints(long userId, PointAwardDto awardDto) throws Exception {
        long teamId = getApiService().getProfileService().findCurrentTeamOfUser(userId).getTeamId();
        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_COMPENSATE_POINTS)
                .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                .put(Constants.FIELD_USER, userId)
                .put(Constants.FIELD_TEAM, teamId)
                .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                .put("amount", awardDto.getAmount())
                .put("tag", String.valueOf(awardDto.getByUser()))
                .build();

        getApiService().getEventService().submitEvent(data);
    }

    @Override
    public void awardBadge(long userId, BadgeAwardDto awardDto) throws Exception {
        long teamId = getApiService().getProfileService().findCurrentTeamOfUser(userId).getTeamId();
        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_AWARD_BADGE)
                .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                .put(Constants.FIELD_USER, userId)
                .put(Constants.FIELD_TEAM, teamId)
                .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                .put("badge", awardDto.getBadgeId())
                .put("subBadge", awardDto.getSubBadgeId())
                .put("tag", String.valueOf(awardDto.getByUser()))
                .build();

        getApiService().getEventService().submitEvent(data);
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
            throw new IOException("No item is found by id " + itemId + "!");
        }
    }

    @Override
    public void buyItem(long userBy, long itemId, float price) throws Exception {
        Map<String, Object> data = Maps.create().put("userId", userBy)
                .put("itemId", itemId)
                .put("cost", price)
                .build();
        getDao().executeCommand("def/item/buyItem", data);
    }

    @Override
    public void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception {
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

            } else {
                throw new IOException("Cannot share this item! Maybe the item itself is shared to you by friend!");
            }
        });

    }

    @Override
    public void readGameTimeline(long since) {

    }

    @Override
    public LeaderboardResponseDto readLeaderboardStatus(LeaderboardRequestDto request) throws Exception {
        String scriptName = String.format("leaderboard/%s",
                request.getType().isCustom() ? "customRange" : "currentRange");

        List<LeaderboardRecordDto> recordDtos = toList(getDao().executeQuery(scriptName,
                Maps.create()
                    .put("startRange", request.getRangeStart())
                    .put("endRange", request.getRangeEnd())
                    .put("timePattern", request.getType().getPattern())
                    .put("topN", request.getTopN())
                    .put("bottomN", request.getBottomN())
                    .build(),
                LeaderboardRecordDto.class,
                Maps.create()
                    .put("special", request.getType().isCustom())
                    .put("teamWise", request.isTeamWise())
                    .put("teamScopeWise", request.isTeamScopeWise())
                    .put("topN", request.getTopN() != null && request.getTopN() > 0)
                    .put("bottomN", request.getBottomN() != null && request.getBottomN() > 0)
                    .build()));

        LeaderboardResponseDto responseDto = new LeaderboardResponseDto();
        responseDto.setRankings(recordDtos);
        responseDto.setRequest(request);
        return responseDto;
    }

}
