package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.injector.model.BadgeModel;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.Map;

/**
 * @author iweerarathna
 */
class BadgeConsumer extends BaseConsumer<BadgeModel> {

    BadgeConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, BadgeModel.class, contextInfo);
    }

    @Override
    public boolean handle(BadgeModel msg) {
        Map<String, Object> map = ConsumerUtils.toBadgeDaoData(contextInfo.getGameId(), msg);

        try {
            dao.executeCommand("game/addBadge", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
