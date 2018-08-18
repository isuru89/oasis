package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.handlers.OStateNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PointsFromStateMapper implements FlatMapFunction<OStateNotification, PointNotification> {

    private List<PointRule> pointRules;
    private transient Map<Long, Map<Integer, Double>> cache;

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
            if (firstRuleOpt.isPresent()) {
                PointRule pointRule = firstRuleOpt.get();

                // calculate point diff
                Map<Integer, Double> pointMap = buildCacheFor(value.getStateRef());
                double points = pointMap.get(value.getState().getId()) - pointMap.get(value.getPreviousState());

                out.collect(new PointNotification(
                        value.getUserId(),
                        Collections.singletonList(value.getEvent()),
                        pointRule,
                        points
                ));
            }
        }
    }

    private synchronized Map<Integer, Double> buildCacheFor(OState state) {
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
        }
        return cache.computeIfAbsent(state.getId(), id -> {
            Map<Integer, Double> pointMap = new ConcurrentHashMap<>();
            state.getStates().forEach(s -> pointMap.put(s.getId(), s.getPoints()));
            return pointMap;
        });
    }

    private String generatePointRuleName(OState state) {
        return String.format("state-change-%s", state.getName());
    }
}
