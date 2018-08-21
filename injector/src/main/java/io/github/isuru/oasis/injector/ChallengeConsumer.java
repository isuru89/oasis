package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.injector.model.ChallengeModel;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.Map;

/**
 * @author iweerarathna
 */
class ChallengeConsumer extends BaseConsumer<ChallengeModel> {

    ChallengeConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, ChallengeModel.class, contextInfo);
    }

    @Override
    public boolean handle(ChallengeModel msg) {
        Map<String, Object> map = ConsumerUtils.toChallengeDaoData(contextInfo.getGameId(), msg);

        try {
            dao.executeCommand("game/addChallengeWinner", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
