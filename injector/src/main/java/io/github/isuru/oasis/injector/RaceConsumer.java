package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.RaceModel;

import java.util.Map;

public class RaceConsumer extends BaseConsumer<RaceModel> {

    private static final String GAME_ADD_RACE_WIN = "game/batch/addRaceAward";

    RaceConsumer(Channel channel, IOasisDao dao, ContextInfo context) {
        super(channel, dao, RaceModel.class, context);
    }

    @Override
    public Map<String, Object> handle(RaceModel msg) {
        return ConsumerUtils.toRaceData(contextInfo.getGameId(), msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_RACE_WIN;
    }
}
