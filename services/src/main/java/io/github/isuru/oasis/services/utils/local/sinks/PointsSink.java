package io.github.isuru.oasis.services.utils.local.sinks;

import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.injector.model.PointModel;
import io.github.isuru.oasis.model.db.IOasisDao;

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
        Map<String, Object> data = ConsumerUtils.toPointDaoData(getGameId(), model);
        dao.executeCommand("game/addPoint", data);
    }
}
