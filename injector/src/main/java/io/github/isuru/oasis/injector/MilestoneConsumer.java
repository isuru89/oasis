package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;

import java.util.Map;

/**
 * @author iweerarathna
 */
class MilestoneConsumer extends BaseConsumer<MilestoneModel> {

    private static final String GAME_ADD_MILESTONE = "game/batch/addMilestone";

    MilestoneConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, MilestoneModel.class, contextInfo);
    }

    @Override
    public Map<String, Object> handle(MilestoneModel msg) {
        return ConsumerUtils.toMilestoneDaoData(contextInfo.getGameId(), msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_MILESTONE;
    }
}
