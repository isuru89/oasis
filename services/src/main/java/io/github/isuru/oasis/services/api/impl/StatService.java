package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IStatService;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.BadgeRecordDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.PointRecordDto;
import io.github.isuru.oasis.services.api.dto.UserBadgeStatDto;
import io.github.isuru.oasis.services.api.dto.UserMilestoneStatDto;
import io.github.isuru.oasis.services.api.dto.UserStatDto;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;

import java.beans.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class StatService extends BaseService implements IStatService {

    StatService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);
    }

    @Override
    public PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception {
        Checks.greaterThanZero(request.getUserId(), "userId");
        Checks.greaterThanZero(request.getPointId(), "pointId");

        Map<String, Object> conditions = Maps.create()
                .put("hasOffset", isValid(request.getOffset()))
                .put("hasSize", isValid(request.getSize()))
                .put("hasRangeStart", isValid(request.getRangeStart()))
                .put("hasRangeEnd", isValid(request.getRangeEnd()))
                .build();

        List<PointRecordDto> recordDtos = toList(getDao().executeQuery("profile/stats/getUserPointsList",
                Maps.create().put("userId", request.getUserId())
                    .put("pointId", request.getPointId())
                    .put("offset", orDefault(request.getOffset(), 0))
                    .put("size", orDefault(request.getSize(), 100))
                    .put("rangeStart", orDefault(request.getRangeStart(), 0L))
                    .put("rangeEnd", orDefault(request.getRangeEnd(), 0L))
                    .build(),
                PointRecordDto.class,
                conditions));

        PointBreakdownResDto resDto = new PointBreakdownResDto();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception {
        Checks.greaterThanZero(request.getUserId(), "userId");

        Map<String, Object> conditions = Maps.create()
                .put("hasBadgeId", isValid(request.getBadgeId()))
                .put("hasOffset", isValid(request.getOffset()))
                .put("hasSize", isValid(request.getSize()))
                .put("hasRangeStart", isValid(request.getRangeStart()))
                .put("hasRangeEnd", isValid(request.getRangeEnd()))
                .build();

        List<BadgeRecordDto> recordDtos = toList(getDao().executeQuery("profile/stats/getUserBadgesList",
                Maps.create().put("userId", request.getUserId())
                        .put("badgeId", orDefault(request.getBadgeId(), 0))
                        .put("offset", orDefault(request.getOffset(), 0))
                        .put("size", orDefault(request.getSize(), 100))
                        .put("rangeStart", orDefault(request.getRangeStart(), 0L))
                        .put("rangeEnd", orDefault(request.getRangeEnd(), 0L))
                        .build(),
                BadgeRecordDto.class,
                conditions));

        BadgeBreakdownResDto resDto = new BadgeBreakdownResDto();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public UserStatDto readUserGameStats(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        Iterable<Map<String, Object>> summaryStat = getDao().executeQuery(
                "profile/stats/getUserStatSummary",
                Maps.create()
                        .put("userId", userId)
                        .put("since", since)
                        .build(),
                tdata);

        UserStatDto dto = new UserStatDto();
        dto.setUserId((int)userId);
        for (Map<String, Object> row : summaryStat) {
            Statement stmt = new Statement(dto,
                    "set" + row.get("type").toString(),
                    new Object[] { firstNonNull(row.get("value_i"), row.get("value_f")) });
            stmt.execute();
        }
        return dto;
    }

    @Override
    public List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        return toList(getDao().executeQuery("profile/stats/getPurchasedItems",
                Maps.create()
                    .put("userId", userId).put("since", since)
                    .build(),
                PurchasedItem.class,
                tdata));
    }

    @Override
    public void readUserGameTimeline(long userId) {

    }

    @Override
    public List<UserBadgeStatDto> readUserBadges(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        return toList(getDao().executeQuery(
                "profile/stats/getUserBadgeStat",
                Maps.create().put("userId", userId).put("since", since).build(),
                UserBadgeStatDto.class,
                tdata));
    }

    @Override
    public List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return toList(getDao().executeQuery(
                "profile/stats/getUserMilestoneStat",
                Maps.create("userId", userId),
                UserMilestoneStatDto.class));
    }

    @Override
    public void readUserRankings(long userId) {

    }

    private Object firstNonNull(Object... vals) {
        for (Object o : vals) {
            if (o != null) {
                return o;
            }
        }
        return null;
    }
}
