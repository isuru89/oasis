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
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;

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
    public void readUserGameStats(long userId) {

    }

    @Override
    public void readUserGameTimeline(long userId) {

    }

    @Override
    public void readUserBadges(long userId) {

    }

    @Override
    public void readUserPoints(long userId) {

    }

    @Override
    public void readUserMilestones(long userId) {

    }

    @Override
    public void readUserRankings(long userId) {

    }
}
