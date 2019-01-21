package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;

import java.util.Map;

/**
 * @author iweerarathna
 */
class MilestoneConsumer extends BaseConsumer<MilestoneModel> {

    MilestoneConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, MilestoneModel.class, contextInfo);
    }

    @Override
    public boolean handle(MilestoneModel msg) {
        Map<String, Object> map = ConsumerUtils.toMilestoneDaoData(contextInfo.getGameId(), msg);

        try {
            dao.executeCommand("game/addMilestone", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
