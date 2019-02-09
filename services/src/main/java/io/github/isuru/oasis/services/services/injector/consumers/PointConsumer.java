package io.github.isuru.oasis.services.services.injector.consumers;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.PointModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointConsumer extends BaseConsumer<PointModel> {

    private static final String GAME_ADD_POINT = "game/batch/addPoint";

    public PointConsumer(Channel channel, IOasisDao dao, ConsumerContext contextInfo) {
        super(channel, dao, PointModel.class, contextInfo);
    }

    @Override
    public Map<String, Object> handle(PointModel msg) {
        return ConsumerUtils.toPointDaoData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_POINT;
    }
}
