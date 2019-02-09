package io.github.isuru.oasis.services.services.injector.consumers;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.OStateModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;

import java.util.Map;

public class StateConsumer extends BaseConsumer<OStateModel> {

    private static final String GAME_UPDATE_STATE = "game/updateState";

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param dao
     * @param context
     */
    public StateConsumer(Channel channel, IOasisDao dao, ConsumerContext context) {
        super(channel, dao, OStateModel.class, context);
    }

    @Override
    public Map<String, Object> handle(OStateModel msg) {
        return ConsumerUtils.toStateDaoData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_UPDATE_STATE;
    }
}
