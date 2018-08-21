package io.github.isuru.oasis.services.utils.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.injector.model.BadgeModel;
import io.github.isuru.oasis.injector.model.ChallengeModel;
import io.github.isuru.oasis.injector.model.MilestoneModel;
import io.github.isuru.oasis.injector.model.MilestoneStateModel;
import io.github.isuru.oasis.injector.model.OStateModel;
import io.github.isuru.oasis.injector.model.PointModel;
import io.github.isuru.oasis.model.db.IOasisDao;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.Serializable;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class DbSink extends OasisSink {

    private static final ObjectMapper mapper = new ObjectMapper();

    private BadgeSink badgeSink;
    private PointsSink pointsSink;
    private ChallengeSink challengeSink;
    private MilestoneSink milestoneSink;
    private MilestoneStateSink milestoneStateSink;
    private StateSink stateSink;

    DbSink(IOasisDao dao, long gameId) {
        badgeSink = new BadgeSink(dao, gameId);
        pointsSink = new PointsSink(dao, gameId);
        challengeSink = new ChallengeSink(dao, gameId);
        milestoneSink = new MilestoneSink(dao, gameId);
        milestoneStateSink = new MilestoneStateSink(dao, gameId);
        stateSink = new StateSink(dao, gameId);
    }

    @Override
    public SinkFunction<String> createPointSink() {
        return pointsSink;
    }

    @Override
    public SinkFunction<String> createMilestoneSink() {
        return milestoneSink;
    }

    @Override
    public SinkFunction<String> createMilestoneStateSink() {
        return milestoneStateSink;
    }

    @Override
    public SinkFunction<String> createBadgeSink() {
        return badgeSink;
    }

    @Override
    public SinkFunction<String> createChallengeSink() {
        return challengeSink;
    }

    @Override
    public SinkFunction<String> createStatesSink() {
        return stateSink;
    }

    private static abstract class BaseLocalSink implements SinkFunction<String>, Serializable {
        protected IOasisDao dao;
        private final long gameId;

        BaseLocalSink(IOasisDao dao, long gameId) {
            this.dao = dao;
            this.gameId = gameId;
        }

        long getGameId() {
            return gameId;
        }
    }

    private static class BadgeSink extends BaseLocalSink {

        BadgeSink(IOasisDao dao, long gameId) {
            super(dao, gameId);
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            BadgeModel badgeModel = mapper.readValue(value, BadgeModel.class);
            Map<String, Object> data = ConsumerUtils.toBadgeDaoData(getGameId(), badgeModel);
            dao.executeCommand("game/addBadge", data);
        }
    }

    private static class PointsSink extends BaseLocalSink {

        PointsSink(IOasisDao dao, long gameId) {
            super(dao, gameId);
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            PointModel model = mapper.readValue(value, PointModel.class);
            Map<String, Object> data = ConsumerUtils.toPointDaoData(getGameId(), model);
            dao.executeCommand("game/addPoint", data);
        }
    }

    private static class MilestoneSink extends BaseLocalSink {

        MilestoneSink(IOasisDao dao, long gameId) {
            super(dao, gameId);
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            MilestoneModel model = mapper.readValue(value, MilestoneModel.class);
            Map<String, Object> data = ConsumerUtils.toMilestoneDaoData(getGameId(), model);
            dao.executeCommand("game/addMilestone", data);
        }
    }

    private static class MilestoneStateSink extends BaseLocalSink {

        MilestoneStateSink(IOasisDao dao, long gameId) {
            super(dao, gameId);
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            MilestoneStateModel model = mapper.readValue(value, MilestoneStateModel.class);
            Map<String, Object> data;
            if (model.isLoss()) {
                data = ConsumerUtils.toMilestoneLossStateDaoData(getGameId(), model);
                dao.executeCommand("game/updateMilestoneStateLoss", data);
            } else {
                data = ConsumerUtils.toMilestoneStateDaoData(getGameId(), model);
                dao.executeCommand("game/updateMilestoneState", data);
            }
        }
    }

    private static class StateSink extends BaseLocalSink {

        StateSink(IOasisDao dao, long gameId) {
            super(dao, gameId);
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            OStateModel model = mapper.readValue(value, OStateModel.class);
            Map<String, Object> data = ConsumerUtils.toStateDaoData(getGameId(), model);
            dao.executeCommand("game/updateState", data);
        }
    }

    private static class ChallengeSink extends BaseLocalSink {

        ChallengeSink(IOasisDao dao, long gameId) {
            super(dao, gameId);
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            ChallengeModel model = mapper.readValue(value, ChallengeModel.class);
            Map<String, Object> data = ConsumerUtils.toChallengeDaoData(getGameId(), model);
            dao.executeCommand("game/addChallengeWinner", data);
        }
    }
}
