package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.db.IOasisDao;
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
    public MilestoneStateConsumer(Channel channel, IOasisDao dao) {
        super(channel, dao, MilestoneStateModel.class);
    }

    @Override
    boolean handle(MilestoneStateModel msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", msg.getUserId());
        map.put("milestoneId", msg.getMilestoneId());
        map.put("valueDouble", msg.getValue());
        map.put("valueLong", msg.getValueInt() == Long.MIN_VALUE ? null : msg.getValueInt());
        map.put("nextVal", msg.getNextValue());
        map.put("nextValInt", msg.getNextValueInt());

        try {
            dao.executeCommand("game/updateMilestoneState", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
