package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.PointAwardDto;
import io.github.isuru.oasis.services.utils.Maps;

import java.io.IOException;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class GameService extends BaseService implements IGameService {

    private final IOasisApiService apiService;

    GameService(IOasisDao oasisDao, IOasisApiService apiService) {
        super(oasisDao);

        this.apiService = apiService;
    }

    @Override
    public void awardPoints(long userId, PointAwardDto awardDto) throws Exception {
        long teamId = apiService.getProfileService().findCurrentTeamOfUser(userId).getTeamId();
        Map<String, Object> data = Maps.create()
                .put("userId", userId)
                .put("teamId", teamId)
                .put("eventType", null)
                .put("extId", null)
                .put("ts", System.currentTimeMillis())
                .put("pointId", 0) // @TODO create award type point
                .put("subPointId", "MANUAL_AWARD")
                .put("points", awardDto.getAmount())
                .put("tag", String.valueOf(awardDto.getByUser()))
                .build();

        getDao().executeCommand("game/addPoint", data);
    }

    @Override
    public void awardBadge(long userId, BadgeAwardDto awardDto) throws Exception {
        long teamId = apiService.getProfileService().findCurrentTeamOfUser(userId).getTeamId();
        Map<String, Object> data = Maps.create()
                .put("userId", userId)
                .put("teamId", teamId)
                .put("eventType", null)
                .put("extId", null)
                .put("ts", System.currentTimeMillis())
                .put("badgeId", awardDto.getBadgeId())
                .put("subBadgeId", awardDto.getSubBadgeId())
                .put("startExtId", null)
                .put("endExtId", null)
                .put("startTime", null)
                .put("endTime", null)
                .put("tag", String.valueOf(awardDto.getByUser()))
                .build();

        getDao().executeCommand("game/addBadge", data);
    }

    @Override
    public void postAChallenge(ChallengeDef challengeDef, boolean startImmediate) throws Exception {
        //startDef(challengeDef.getId(), false);
    }

    @Override
    public void buyItem(long userBy, long itemId) throws Exception {
        ShopItem shopItem = apiService.getGameDefService().readShopItem(itemId);
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
        Map<String, Object> data = Maps.create().put("userId", userBy)
                .put("itemId", itemId)
                .put("currentEpoch", System.currentTimeMillis())
                .put("amount", amount)
                .build();
        long l = getDao().executeCommand("def/item/shareItem", data);
        if (l == amount) {
            Map<String, Object> item = Maps.create().put("userId", toUser)
                    .put("itemId", itemId)
                    .build();
            getDao().executeCommand("def/item/shareToItem", item);

        } else {
            throw new IOException("Cannot share this item! Maybe the item itself is shared to you by friend!");
        }
    }

    @Override
    public void readGameTimeline(long since) {

    }

    @Override
    public void readLeaderboardStatus(long leaderboardId) {

    }

}
