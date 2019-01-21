package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.MilestoneStateModel;

import java.util.Map;

/**
 * @author iweerarathna
 */
class MilestoneStateConsumer extends BaseConsumer<MilestoneStateModel> {

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param dao
     */
    MilestoneStateConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, MilestoneStateModel.class, contextInfo);
    }

    @Override
    public boolean handle(MilestoneStateModel msg) {
        String qId;
        Map<String, Object> map;

        if (msg.getLossUpdate() != null && msg.getLossUpdate()) {
            map = ConsumerUtils.toMilestoneLossStateDaoData(contextInfo.getGameId(), msg);

            qId = "game/updateMilestoneStateLoss";

        } else {
            map = ConsumerUtils.toMilestoneStateDaoData(contextInfo.getGameId(), msg);

            qId = "game/updateMilestoneState";
        }

        try {
            dao.executeCommand(qId, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
