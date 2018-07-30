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
        return EventNames.START_CHALLENGE.equals(value.getEventType())
                || Utils.isNullOrEmpty(challengeDef.getForEvents())
                || challengeDef.getForEvents().contains(value.getEventType());
    }

    public static boolean filter(Event event, ChallengeDef challengeDef) throws IOException {
        if (challengeDef.getForEvents().contains(event.getEventType())) {
            List<String> conditions = challengeDef.getConditions();
            for (String c : conditions) {
                if (Utils.evaluateCondition(c, event.getAllFieldValues())) {
                    return true;
                }
            }
        }
        return false;
    }
}
