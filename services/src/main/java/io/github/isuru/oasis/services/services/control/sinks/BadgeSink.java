package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;
import io.github.isuru.oasis.services.services.injector.consumers.ConsumerUtils;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class BadgeSink extends BaseLocalSink {

    BadgeSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_BADGES);
    }

    @Override
    protected void handle(String value) throws Exception {
        BadgeModel badgeModel = mapper.readValue(value, BadgeModel.class);
        Map<String, Object> data = ConsumerUtils.toBadgeDaoData(badgeModel);
        dao.executeCommand("game/addBadge", data);
    }
}
