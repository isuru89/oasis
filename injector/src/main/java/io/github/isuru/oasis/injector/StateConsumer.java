package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.injector.model.OStateModel;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.HashMap;
import java.util.Map;

public class StateConsumer extends BaseConsumer<OStateModel> {

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param dao
     * @param context
     */
    public StateConsumer(Channel channel, IOasisDao dao, ContextInfo context) {
        super(channel, dao, OStateModel.class, context);
    }

    @Override
    boolean handle(OStateModel msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", msg.getUserId());
        map.put("stateId", msg.getStateId());
        map.put("currState", msg.getCurrentState());
        map.put("currValue", msg.getCurrentValue());
        map.put("extId", msg.getExtId());
        map.put("ts", msg.getTs());
        map.put("gameId", contextInfo.getGameId());

        try {
            dao.executeCommand("game/updateState", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
