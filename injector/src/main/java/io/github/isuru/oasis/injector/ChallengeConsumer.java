package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.injector.model.ChallengeModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
class ChallengeConsumer extends BaseConsumer<ChallengeModel> {

    ChallengeConsumer(Channel channel, IOasisDao dao) {
        super(channel, dao, ChallengeModel.class);
    }

    @Override
    boolean handle(ChallengeModel msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", msg.getUserId());
        map.put("teamId", msg.getTeamId());
        map.put("teamScopeId", msg.getTeamScopeId());
        map.put("challengeId", msg.getChallengeId());
        map.put("points", msg.getPoints());
        map.put("wonAt", msg.getWonAt());

        try {
            dao.executeCommand("game/addChallengeWinner", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
