package io.github.isuru.oasis.services.services.injector.consumers;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.services.injector.MsgAcknowledger;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class BadgeConsumer extends BaseConsumer<BadgeModel> {

    private static final String GAME_BATCH_ADD_BADGE = "game/batch/addBadge";

    public BadgeConsumer(IOasisDao dao, ConsumerContext contextInfo, MsgAcknowledger acknowledger) {
        super(dao, BadgeModel.class, contextInfo, acknowledger);
    }

    @Override
    public Map<String, Object> handle(BadgeModel msg) {
        return ConsumerUtils.toBadgeDaoData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_BATCH_ADD_BADGE;
    }

}