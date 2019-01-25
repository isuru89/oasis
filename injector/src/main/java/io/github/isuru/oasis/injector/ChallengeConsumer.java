package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.ChallengeModel;

import java.util.Map;

/**
 * @author iweerarathna
 */
class ChallengeConsumer extends BaseConsumer<ChallengeModel> {

    private static final String GAME_ADD_CHALLENGE_WINNER = "game/batch/addChallengeWinner";

    ChallengeConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, ChallengeModel.class, contextInfo);
    }

    @Override
    public Map<String, Object> handle(ChallengeModel msg) {
        return ConsumerUtils.toChallengeDaoData(contextInfo.getGameId(), msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_CHALLENGE_WINNER;
    }
}
