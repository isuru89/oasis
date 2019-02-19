package io.github.isuru.oasis.services.services.injector.consumers;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.services.injector.MsgAcknowledger;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneConsumer extends BaseConsumer<MilestoneModel> {

    private static final String GAME_ADD_MILESTONE = "game/batch/addMilestone";

    public MilestoneConsumer(IOasisDao dao, ConsumerContext contextInfo, MsgAcknowledger acknowledger) {
        super(dao, MilestoneModel.class, contextInfo, acknowledger);
    }

    @Override
    public Map<String, Object> handle(MilestoneModel msg) {
        return ConsumerUtils.toMilestoneDaoData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_MILESTONE;
    }
}
