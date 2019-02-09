package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.handlers.output.ChallengeModel;
import io.github.isuru.oasis.services.services.injector.consumers.ConsumerUtils;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeSink extends BaseLocalSink {

    ChallengeSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_CHALLENGES);
    }

    @Override
    protected void handle(String value) throws Exception {
        ChallengeModel model = mapper.readValue(value, ChallengeModel.class);
        Map<String, Object> data = ConsumerUtils.toChallengeDaoData(model);
        dao.executeCommand("game/addChallengeWinner", data);
    }
}
