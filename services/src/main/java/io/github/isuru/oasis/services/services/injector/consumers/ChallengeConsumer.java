package io.github.isuru.oasis.services.services.injector.consumers;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.ChallengeModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.services.injector.MsgAcknowledger;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeConsumer extends BaseConsumer<ChallengeModel> {

    private static final String GAME_ADD_CHALLENGE_WINNER = "game/batch/addChallengeWinner";

    public ChallengeConsumer(IOasisDao dao, ConsumerContext contextInfo, MsgAcknowledger acknowledger) {
        super(dao, ChallengeModel.class, contextInfo, acknowledger);
    }

    @Override
    public Map<String, Object> handle(ChallengeModel msg) {
        return ConsumerUtils.toChallengeDaoData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_CHALLENGE_WINNER;
    }
}
