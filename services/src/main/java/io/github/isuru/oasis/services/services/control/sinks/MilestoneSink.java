package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;
import io.github.isuru.oasis.services.services.injector.consumers.ConsumerUtils;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneSink extends BaseLocalSink {

    MilestoneSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_MILESTONES);
    }

    @Override
    protected void handle(String value) throws Exception {
        MilestoneModel model = mapper.readValue(value, MilestoneModel.class);
        Map<String, Object> data = ConsumerUtils.toMilestoneDaoData(model);
        dao.executeCommand("game/addMilestone", data);
    }
}
