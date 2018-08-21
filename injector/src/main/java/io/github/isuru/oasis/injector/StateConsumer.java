package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.injector.model.OStateModel;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.Map;

class StateConsumer extends BaseConsumer<OStateModel> {

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param dao
     * @param context
     */
    StateConsumer(Channel channel, IOasisDao dao, ContextInfo context) {
        super(channel, dao, OStateModel.class, context);
    }

    @Override
    public boolean handle(OStateModel msg) {
        Map<String, Object> map = ConsumerUtils.toStateDaoData(contextInfo.getGameId(), msg);

        try {
            dao.executeCommand("game/updateState", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
