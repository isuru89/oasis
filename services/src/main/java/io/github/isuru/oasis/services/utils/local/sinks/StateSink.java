package io.github.isuru.oasis.services.utils.local.sinks;

import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.injector.model.OStateModel;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class StateSink extends BaseLocalSink {

    StateSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_STATES);
    }

    @Override
    protected void handle(String value) throws Exception {
        OStateModel model = mapper.readValue(value, OStateModel.class);
        Map<String, Object> data = ConsumerUtils.toStateDaoData(getGameId(), model);
        dao.executeCommand("game/updateState", data);
    }
}