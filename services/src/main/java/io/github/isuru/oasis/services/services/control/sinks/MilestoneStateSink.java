package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneStateModel;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneStateSink extends BaseLocalSink {

    MilestoneStateSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_MILESTONE_STATES);
    }

    @Override
    protected void handle(String value) throws Exception {
        MilestoneStateModel model = mapper.readValue(value, MilestoneStateModel.class);
        Map<String, Object> data;
        if (model.getLossUpdate() != null && model.getLossUpdate()) {
            data = ConsumerUtils.toMilestoneLossStateDaoData(getGameId(), model);
            dao.executeCommand("game/updateMilestoneStateLoss", data);
        } else {
            data = ConsumerUtils.toMilestoneStateDaoData(getGameId(), model);
            dao.executeCommand("game/updateMilestoneState", data);
        }
    }
}