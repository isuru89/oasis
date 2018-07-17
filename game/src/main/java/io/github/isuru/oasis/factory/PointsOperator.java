package io.github.isuru.oasis.factory;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.handlers.IErrorHandler;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.utils.Utils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class PointsOperator<IN extends Event> extends RichFlatMapFunction<IN, PointEvent> {

    private Map<String, List<PointRule>> pointRules;
    private List<PointRule> pointSelfRules;
    private IErrorHandler<PointRule> errorHandler;

    public PointsOperator(List<PointRule> rules, IErrorHandler<PointRule> errorHandler) {
        if (rules != null) {
            pointRules = rules.stream()
                    .filter(r -> !"POINTS".equals(r.getSource()))
                    .collect(Collectors.groupingBy(PointRule::getForEvent));
            pointSelfRules = rules.stream().filter(r -> "POINTS".equals(r.getSource()))
                    .collect(Collectors.toCollection(LinkedList::new));
        } else {
            pointRules = new HashMap<>();
            pointSelfRules = new LinkedList<>();
        }
        this.errorHandler = errorHandler;
    }

    @Override
    public void flatMap(IN value, Collector<PointEvent> out) {
        Map<String, Pair<Double, PointRule>> scoredPoints = new HashMap<>();
        double totalPoints = 0.0;
        List<PointRule> rulesForEvent = pointRules.get(value.getEventType());
        if (Utils.isNullOrEmpty(rulesForEvent)) {
            return;
        }

        for (PointRule pointRule : rulesForEvent) {
            try {
                Optional<Double> result = executeRuleConditionAndValue(value, pointRule, value.getAllFieldValues());
                if (result.isPresent()) {
                    double d = result.get();
                    scoredPoints.put(pointRule.getName(), Pair.of(d, pointRule));

                    // calculate points for other users other than main user of this event belongs to
                    calculateRedirectedPoints(value, pointRule, out);
                    totalPoints += d;
                }

            } catch (Throwable t) {
                errorHandler.onError(t, value, pointRule);
            }
        }

        // evaluate aggregated points
        evalSelfRules(value, scoredPoints, totalPoints);

        PointEvent pe = new PointEvent(value);
        pe.setPointEvents(scoredPoints);

        out.collect(pe);
    }

    private synchronized void calculateRedirectedPoints(IN value, PointRule rule, Collector<PointEvent> out) {
        if (Utils.isNonEmpty(rule.getAdditionalPoints())) {
            for (PointRule.AdditionalPointReward reward : rule.getAdditionalPoints()) {
                Long userId = value.getUserId(reward.getToUser());
                if (userId != null && userId > 0) {
                    Double amount = evaluateExpression(reward.getAmount(), value.getAllFieldValues());

                    PointEvent pointEvent = new RedirectedPointEvent(value, reward.getToUser());
                    Map<String, Pair<Double, PointRule>> scoredPoints = new HashMap<>();
                    scoredPoints.put(reward.getName(), Pair.of(amount, rule));
                    pointEvent.setPointEvents(scoredPoints);
                    out.collect(pointEvent);
                }
            }
        }
    }

    private void evalSelfRules(IN value, Map<String, Pair<Double, PointRule>> points, double totalPoints) {
        if (Utils.isNonEmpty(pointSelfRules)) {
            Map<String, Object> vars = new HashMap<>(value.getAllFieldValues());
            vars.put("$TOTAL", totalPoints);

            for (PointRule rule : pointSelfRules) {
                if (rule.getForEvent().equals(value.getEventType())) {
                    try {
                        executeRuleConditionAndValue(value, rule, vars)
                                .ifPresent(p -> points.put(rule.getName(), Pair.of(p, rule)));

                    } catch (Throwable t) {
                        errorHandler.onError(t, value, rule);
                    }
                }
            }
        }
    }

    private Optional<Double> executeRuleConditionAndValue(IN value, PointRule rule, Map<String, Object> variables) throws IOException {
        Boolean status;
//        if (rule.getConditionClass() != null) {
//            try {
//                status = rule.getConditionClass().filter(value);
//            } catch (Exception e) {
//                throw new IOException("Failed to evaluate condition expression for rule '" + rule.getId() + "'!", e);
//            }
//        } else {
//
//        }
        status = Utils.evaluateCondition(rule.getConditionExpression(), variables);

        if (status == Boolean.TRUE) {
            double p;
            if (rule.getAmountExpression() != null) {
                p = evaluateAmount(rule, variables);
            } else {
                p = rule.getAmount();
            }
            return Optional.of(p);
        }
        return Optional.empty();
    }

    private static Double evaluateExpression(Serializable expr, Map<String, Object> vars) {
        if (expr instanceof Number) {
            return Utils.toDouble((Number)expr);
        } else {
            Object o = MVEL.executeExpression(expr, vars);
            if (o instanceof Number) {
                return Utils.toDouble((Number) o);
            } else {
                return 0.0;
            }
        }
    }

    private static Double evaluateAmount(PointRule rule, Map<String, Object> vars) {
        return evaluateExpression(rule.getAmountExpression(), vars);
    }

    public static class RedirectedPointEvent extends PointEvent {

        private String ownUserField;

        RedirectedPointEvent(Event event, String userFieldId) {
            super(event);
            this.ownUserField = userFieldId;
        }

        @Override
        public long getUser() {
            return getUserId(ownUserField);
        }
    }

}
