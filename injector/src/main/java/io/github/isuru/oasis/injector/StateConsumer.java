package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.OStateModel;

import java.util.Map;

class StateConsumer extends BaseConsumer<OStateModel> {

    private static final String GAME_UPDATE_STATE = "game/updateState";

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
    public Map<String, Object> handle(OStateModel msg) {
        return ConsumerUtils.toStateDaoData(contextInfo.getGameId(), msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_UPDATE_STATE;
    }
}
