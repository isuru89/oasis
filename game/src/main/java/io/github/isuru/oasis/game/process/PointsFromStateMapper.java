package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.handlers.OStateNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.*;

public class PointsFromStateMapper implements FlatMapFunction<OStateNotification, PointNotification> {

    private List<PointRule> pointRules;

    public PointsFromStateMapper(List<PointRule> pointRules) {
        this.pointRules = pointRules != null ? pointRules : new LinkedList<>();
    }

    @Override
    public void flatMap(OStateNotification value, Collector<PointNotification> out) {
        if (value.getState().getPoints() != null
                && Integer.compare(value.getPreviousState(), value.getState().getId()) != 0) {
            // state change, calculate necessary points to award.
            String ruleName = generatePointRuleName(value.getStateRef());
            Optional<PointRule> firstRuleOpt = pointRules.stream()
                    .filter(pr -> pr.getName().equals(ruleName))
                    .findFirst();
            firstRuleOpt.ifPresent(pointRule -> out.collect(new PointNotification(
                    value.getUserId(),
                    Collections.singletonList(value.getEvent()),
                    pointRule,
                    value.getState().getPoints()
            )));
        }
    }

    private String generatePointRuleName(OState state) {
        return String.format("state-change-%s", state.getName());
    }
}
