package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.OStateModel;
import io.github.isuru.oasis.services.services.injector.consumers.ConsumerUtils;

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
        Map<String, Object> data = ConsumerUtils.toStateDaoData(model);
        dao.executeCommand("game/updateState", data);
    }
}