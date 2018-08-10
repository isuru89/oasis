package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.services.api.dto.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.UserBadgeStatDto;
import io.github.isuru.oasis.services.api.dto.UserMilestoneStatDto;
import io.github.isuru.oasis.services.api.dto.UserStatDto;
import io.github.isuru.oasis.services.model.PurchasedItem;

import java.util.List;

/**
 * @author iweerarathna
 */
public interface IStatService {

    PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception;
    BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception;

    UserStatDto readUserGameStats(long userId, long since) throws Exception;
    List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception;
    void readUserGameTimeline(long userId);
    List<UserBadgeStatDto> readUserBadges(long userId, long since) throws Exception;
    List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception;
    void readUserRankings(long userId);

}
