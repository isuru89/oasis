package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author iweerarathna
 */
class BadgeConsumer extends BaseConsumer<BadgeModel> {

    private static final Logger LOG = LoggerFactory.getLogger(BadgeConsumer.class);

    private static final String GAME_BATCH_ADD_BADGE = "game/batch/addBadge";

    BadgeConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, BadgeModel.class, contextInfo);
    }

    @Override
    public Map<String, Object> handle(BadgeModel msg) {
        return ConsumerUtils.toBadgeDaoData(contextInfo.getGameId(), msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_BATCH_ADD_BADGE;
    }

}
