package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.PointModel;
import io.github.isuru.oasis.services.services.injector.consumers.ConsumerUtils;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointsSink extends BaseLocalSink {

    PointsSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_POINTS);
    }

    @Override
    protected void handle(String value) throws Exception {
        PointModel model = mapper.readValue(value, PointModel.class);
        Map<String, Object> data = ConsumerUtils.toPointDaoData(model);
        dao.executeCommand("game/addPoint", data);
    }
}
