package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.injector.model.PointModel;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.Map;

/**
 * @author iweerarathna
 */
class PointConsumer extends BaseConsumer<PointModel> {

    PointConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, PointModel.class, contextInfo);
    }

    @Override
    public boolean handle(PointModel msg) {
        Map<String, Object> map = ConsumerUtils.toPointDaoData(contextInfo.getGameId(), msg);

        try {
            dao.executeCommand("game/addPoint", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
