package io.github.isuru.oasis.services.services.injector.consumers;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneConsumer extends BaseConsumer<MilestoneModel> {

    private static final String GAME_ADD_MILESTONE = "game/batch/addMilestone";

    public MilestoneConsumer(Channel channel, IOasisDao dao, ConsumerContext contextInfo) {
        super(channel, dao, MilestoneModel.class, contextInfo);
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
