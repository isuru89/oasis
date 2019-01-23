package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.utils.Commons;
import org.apache.commons.lang3.BooleanUtils;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.*;

public class ChallengeCheck implements Serializable {

    private static final ChallengeFilterResult CONTINUE =
            new ChallengeFilterResult(false, true);
    private static final ChallengeFilterResult HALT =
            new ChallengeFilterResult(false, false);

    private final ChallengeDef def;
    private final Set<String> eventNames;
    private final List<Serializable> conditions;
    private int winners;

    ChallengeCheck(ChallengeDef def) {
        this.def = def;
        this.eventNames = new HashSet<>(def.getForEvents());
        this.winners = 0;

        this.conditions = new LinkedList<>();
        if (!Commons.isNullOrEmpty(def.getConditions())) {
            for (String expr : def.getConditions()) {
                this.conditions.add(MVEL.compileExpression(expr));
            }
        }
    }

    public ChallengeFilterResult check(Event event) {
        if (!eventNames.contains(event.getEventType())) {
            return CONTINUE;
        }

        // check for expiration
        if (event.getTimestamp() > def.getExpireAfter()) {
            return HALT;
        } else if (event.getTimestamp() < def.getStartAt()) {
            return CONTINUE;
        }

        // check user match
        if (def.getForUserId() != null && event.getUser() != def.getForUserId()) {
            return CONTINUE;
        }

        // check team match
        if (def.getForTeamId() != null && !def.getForTeamId().equals(event.getTeam())) {
            return CONTINUE;
        }

        // check team-scope match
        if (def.getForTeamScopeId() != null && def.getForTeamScopeId().equals(event.getTeamScope())) {
            return CONTINUE;
        }

        boolean satisfied = conditions.size() == 0;
        Map<String, Object> variables = new HashMap<>(event.getAllFieldValues());
        for (Serializable expr : conditions) {
            if (interpretCondition(MVEL.executeExpression(expr, variables))) {
                satisfied = true;
                break;
            }
        }

        if (satisfied) {
            winners++;
        }

        boolean canContinue = def.getWinnerCount() > winners;
        return new ChallengeFilterResult(satisfied, canContinue);
    }

    private boolean interpretCondition(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) {
            return (boolean) val;
        } else if (val instanceof Number) {
            return ((Number) val).longValue() > 0;
        }
        return BooleanUtils.toBoolean(val.toString());
    }


    static class ChallengeFilterResult {
        private final boolean satisfied;
        private final boolean isContinue;

        private ChallengeFilterResult(boolean satisfied, boolean isContinue) {
            this.satisfied = satisfied;
            this.isContinue = isContinue;
        }

        boolean isSatisfied() {
            return satisfied;
        }

        boolean isContinue() {
            return isContinue;
        }
    }

}
