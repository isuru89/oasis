package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.services.api.dto.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownResDto;

/**
 * @author iweerarathna
 */
public interface IStatService {

    PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception;
    BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception;

    void readUserGameStats(long userId);
    void readUserGameTimeline(long userId);
    void readUserBadges(long userId);
    void readUserPoints(long userId);
    void readUserMilestones(long userId);
    void readUserRankings(long userId);

}
