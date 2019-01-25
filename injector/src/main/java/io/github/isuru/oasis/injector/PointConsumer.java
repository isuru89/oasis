package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.PointModel;

import java.util.Map;

/**
 * @author iweerarathna
 */
class PointConsumer extends BaseConsumer<PointModel> {

    private static final String GAME_ADD_POINT = "game/batch/addPoint";

    PointConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, PointModel.class, contextInfo);
    }

    @Override
    public Map<String, Object> handle(PointModel msg) {
        return ConsumerUtils.toPointDaoData(contextInfo.getGameId(), msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_POINT;
    }
}
