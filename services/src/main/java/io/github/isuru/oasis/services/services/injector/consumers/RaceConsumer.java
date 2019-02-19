package io.github.isuru.oasis.services.services.injector.consumers;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.RaceModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.services.injector.MsgAcknowledger;

import java.util.Map;

public class RaceConsumer extends BaseConsumer<RaceModel> {

    private static final String GAME_ADD_RACE_WIN = "game/batch/addRaceAward";

    public RaceConsumer(IOasisDao dao, ConsumerContext context, MsgAcknowledger acknowledger) {
        super(dao, RaceModel.class, context, acknowledger);
    }

    @Override
    public Map<String, Object> handle(RaceModel msg) {
        return ConsumerUtils.toRaceData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_RACE_WIN;
    }
}
