package io.github.isuru.oasis.game.process.challenge;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.EventNames;
import org.apache.flink.api.common.functions.FilterFunction;

import java.io.IOException;
import java.util.List;

/**
 * @author iweerarathna
 */
public class ChallengeFilter implements FilterFunction<Event> {

    private ChallengeDef challengeDef;

    public ChallengeFilter(ChallengeDef def) {
        this.challengeDef = def;
    }

    @Override
    public boolean filter(Event value) {
        if (Utils.eventEquals(value, EventNames.START_CHALLENGE)
                || Utils.isNullOrEmpty(challengeDef.getForEvents())
                || challengeDef.getForEvents().contains(value.getEventType())) {
            return userFilterSuccess(challengeDef, value);
        } else {
            return false;
        }
    }

    private boolean userFilterSuccess(ChallengeDef def, Event event) {
        if (def.getForUserId() != null) {
            return event.getUser() == def.getForUserId();
        } else if (def.getForTeamId() != null) {
            return def.getForTeamId().equals(event.getTeam());
        } else if (def.getForTeamScopeId() != null) {
            return def.getForTeamScopeId().equals(event.getTeamScope());
        } else {
            return true;
        }
    }

    public static boolean filter(Event event, ChallengeDef challengeDef) throws IOException {
        if (challengeDef.getForEvents().contains(event.getEventType())) {
            List<String> conditions = challengeDef.getConditions();
            for (String c : conditions) {
                if (Utils.evaluateCondition(Utils.compileExpression(c), event.getAllFieldValues())) {
                    return true;
                }
            }
        }
        return false;
    }
}
