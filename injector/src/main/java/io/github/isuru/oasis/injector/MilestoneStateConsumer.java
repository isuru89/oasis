package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.injector.model.MilestoneStateModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneStateConsumer extends BaseConsumer<MilestoneStateModel> {

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param dao
     */
    public MilestoneStateConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, MilestoneStateModel.class, contextInfo);
    }

    @Override
    boolean handle(MilestoneStateModel msg) {
        String qId;
        Map<String, Object> map = new HashMap<>();

        if (msg.getLossUpdate() != null && msg.getLossUpdate()) {
            map.put("userId", msg.getUserId());
            map.put("milestoneId", msg.getMilestoneId());
            map.put("lossVal", msg.getLossValue());
            map.put("lossValInt", msg.getLossValueInt());

            qId = "game/updateMilestoneStateLoss";

        } else {
            map.put("userId", msg.getUserId());
            map.put("milestoneId", msg.getMilestoneId());
            map.put("valueDouble", msg.getValue());
            map.put("valueLong", msg.getValueInt() == Long.MIN_VALUE ? null : msg.getValueInt());
            map.put("nextVal", msg.getNextValue());
            map.put("nextValInt", msg.getNextValueInt());
            map.put("gameId", contextInfo.getGameId());

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
