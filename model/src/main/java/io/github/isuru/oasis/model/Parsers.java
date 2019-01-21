package io.github.isuru.oasis.model;

import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.model.rules.*;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parsers {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9\\\\.]+)\\s*([kKmMbB]?)");

    public static FieldCalculator parse(KpiDef kpiDef) {
        FieldCalculator calculator = new FieldCalculator();
        calculator.setId(kpiDef.getId());
        calculator.setPriority(Integer.parseInt(kpiDef.getId().toString()));
        calculator.setForEvent(kpiDef.getEvent());
        calculator.setFieldName(kpiDef.getField());
        calculator.setExpression(compileExpression(kpiDef.getExpression()));

        return calculator;
    }

    public static PointRule parse(PointDef record) throws IllegalArgumentException {
        PointRule pr = new PointRule();
        pr.setId(record.getId());
        pr.setName(record.getName());
        pr.setForEvent(record.getEvent());
        pr.setSource(record.getSource());
        pr.setCurrency(record.isCurrency());

        Object amount = record.getAmount();
        if (amount != null) {
            if (amount instanceof Number) {
                pr.setAmount(((Number) amount).intValue());
            } else if (amount instanceof String) {
                pr.setAmountExpression(compileExpression(amount.toString()));
            } else {
                throw new IllegalArgumentException("'amount' field missing or does not have type neither number nor string!");
            }
        }

        pr.setCondition(compileExpression(record.getCondition()));

        if (record.getAdditionalPoints() != null) {
            List<PointRule.AdditionalPointReward> rewards = new LinkedList<>();
            for (PointsAdditional pa : record.getAdditionalPoints()) {
                precheck(pa, record);

                PointRule.AdditionalPointReward apr = new PointRule.AdditionalPointReward();
                apr.setToUser(pa.getToUser());
                apr.setName(pa.getName());
                if (pa.getCurrency() == null) {
                    apr.setCurrency(pr.isCurrency());
                } else {
                    apr.setCurrency(pa.getCurrency());
                }

                if (pa.getAmount() instanceof Number) {
                    apr.setAmount((Number) pa.getAmount());
                } else {
                    apr.setAmount(compileExpression(pa.getAmount().toString()));
                }
                rewards.add(apr);
            }
            pr.setAdditionalPoints(rewards);
        }

        return pr;
    }

    public static BadgeRule parse(BadgeDef badgeDef) throws IllegalArgumentException {
        Badge badge = new Badge(badgeDef.getId(), badgeDef.getName());
        badge.setAwardPoints(badgeDef.getAwardPoints());

        BadgeSourceDef from = badgeDef.getFrom();
        if (from != null) {
            if (from.getPointsRef() != null) {
                BadgeFromPoints bp = new BadgeFromPoints();
                bp.setId(badgeDef.getId());
                bp.setMaxBadges(badgeDef.getMaxBadges());
                bp.setPointsId(from.getPointsRef());
                bp.setDuration(from.getWithin());
                bp.setStreak(from.getStreak() != null ? from.getStreak() : 0);
                bp.setBadge(badge);
                bp.setAggregator(from.getAggregator());
                bp.setCondition(from.getCondition());

                if (from.getSubBadges() != null) {
                    List<Badge> subBadges = new LinkedList<>();
                    for (BadgeDef.SubBadgeDef sbd : from.getSubBadges()) {
                        if (sbd.getCondition() != null) {
                            subBadges.add(new BadgeFromEvents.ConditionalSubBadge(sbd.getName(), badge,
                                    compileExpression(sbd.getCondition())));
                        } else {
                            BadgeFromPoints.StreakSubBadge streakSubBadge = new BadgeFromPoints.StreakSubBadge(sbd.getName(), badge, sbd.getStreak());
                            streakSubBadge.setAwardPoints(sbd.getAwardPoints());
                            subBadges.add(streakSubBadge);
                        }
                    }
                    bp.setSubBadges(subBadges);
                }
                return bp;

            } else if (from.getMilestoneRef() != null) {
                BadgeFromMilestone bfm = new BadgeFromMilestone();
                bfm.setId(badgeDef.getId());
                bfm.setMilestoneId(from.getMilestoneRef());
                bfm.setLevel(from.getLevel());
                bfm.setBadge(badge);

                if (from.getSubBadges() != null) {
                    List<BadgeFromMilestone.LevelSubBadge> subBadges = new LinkedList<>();
                    for (BadgeDef.SubBadgeDef sbd : from.getSubBadges()) {
                        BadgeFromMilestone.LevelSubBadge levelSubBadge = new BadgeFromMilestone.LevelSubBadge(sbd.getName(), badge, sbd.getLevel());
                        levelSubBadge.setAwardPoints(sbd.getAwardPoints());
                        subBadges.add(levelSubBadge);
                    }
                    bfm.setSubBadges(subBadges);
                }
                return bfm;

            } else {
                throw new IllegalArgumentException("Unknown source for badge rule from field 'from'!");
            }

        } else {
            BadgeFromEvents bfe = new BadgeFromEvents();
            bfe.setId(badgeDef.getId());
            bfe.setBadge(badge);
            bfe.setEventType(badgeDef.getEvent());
            bfe.setDuration(badgeDef.getWithin());
            bfe.setMaxBadges(badgeDef.getMaxBadges());
            if (badgeDef.getContinuous() != null) {
                bfe.setContinuous(badgeDef.getContinuous());
            }
            if (!isNullOrEmpty(badgeDef.getContinuousAggregator())) {
                bfe.setContinuousAggregator(MVEL.compileExpression(badgeDef.getContinuousAggregator()));
            }
            if (!isNullOrEmpty(badgeDef.getContinuousCondition())) {
                bfe.setContinuousCondition(MVEL.compileExpression(badgeDef.getContinuousCondition()));
            }
            if (badgeDef.getCondition() != null) {
                bfe.setCondition(MVEL.compileExpression(badgeDef.getCondition()));
            }
            if (badgeDef.getStreak() != null) {
                bfe.setStreak(badgeDef.getStreak());
            }

            if (badgeDef.getSubBadges() != null) {
                List<Badge> subBadges = new LinkedList<>();
                for (BadgeDef.SubBadgeDef sbd : badgeDef.getSubBadges()) {
                    if (sbd.getStreak() != null) {
                        BadgeFromPoints.StreakSubBadge streakSubBadge = new BadgeFromPoints.StreakSubBadge(sbd.getName(), badge, sbd.getStreak());
                        streakSubBadge.setAwardPoints(sbd.getAwardPoints());
                        subBadges.add(streakSubBadge);
                    } else if (sbd.getCondition() != null) {
                        Serializable serializable = MVEL.compileExpression(sbd.getCondition());
                        BadgeFromEvents.ConditionalSubBadge conditionalSubBadge = new BadgeFromEvents.ConditionalSubBadge(sbd.getName(), badge, serializable);
                        conditionalSubBadge.setAwardPoints(sbd.getAwardPoints());
                        subBadges.add(conditionalSubBadge);
                    } else if (bfe.isContinuous() && sbd.getWithin() != null) {
                        BadgeFromEvents.ContinuousSubBadge csb = new BadgeFromEvents.ContinuousSubBadge(sbd.getName(), badge, sbd.getWithin());
                        subBadges.add(csb);
                    } else {
                        throw new IllegalArgumentException("Unknown sub badge type! [" + sbd.getId() + "]");
                    }
                }
                bfe.setSubBadges(subBadges);
            }
            return bfe;
        }
    }

    public static Milestone parse(MilestoneDef milestoneDef) throws IllegalArgumentException {
        Milestone milestone = new Milestone();
        milestone.setId(milestoneDef.getId());
        milestone.setName(milestoneDef.getName());
        milestone.setDisplayName(milestoneDef.getDisplayName());
        milestone.setEvent(milestoneDef.getEvent());
        milestone.setFrom(milestoneDef.getFrom());
        milestone.setOnlyPositive(milestoneDef.getOnlyPositive() != null ? milestoneDef.getOnlyPositive() : false);
        if (isNonEmpty(milestoneDef.getPointRefs())) {
            milestone.setPointIds(new HashSet<>(milestoneDef.getPointRefs()));
        }

        String type = milestoneDef.getAggregator() != null
                ? milestoneDef.getAggregator()
                : AggregatorType.COUNT.name();

        milestone.setAggregator(AggregatorType.valueOf(type.toUpperCase()));
        if (milestoneDef.getAccumulator() != null) {
            milestone.setAccumulatorExpr(compileExpression(milestoneDef.getAccumulator()));
        }

        if (milestoneDef.getCondition() != null) {
            milestone.setCondition(compileExpression(milestoneDef.getCondition()));
        }

        List<Milestone.Level> levels = new LinkedList<>();
        int doubleCount = 0;
        boolean asRealValues = "double".equalsIgnoreCase(milestoneDef.getAccumulatorType());
        for (Map.Entry<Integer, Object> entry : milestoneDef.getLevels().entrySet()) {
            Milestone.Level level = new Milestone.Level();
            level.setLevel(entry.getKey());
            Number number = interpretNumber(entry.getValue(), asRealValues);
            level.setNumber(number);
            level.setAwardPoints(milestoneDef.getAwardPoints(entry.getKey()));

            if (Double.class.isAssignableFrom(number.getClass())) {
                doubleCount++;
            }
            levels.add(level);
        }
        milestone.setLevels(levels);

        if (milestoneDef.getAccumulatorType() == null) {
            milestone.setRealValues(doubleCount > 0);
        } else {
            milestone.setRealValues(asRealValues);
        }

        return milestone;
    }

    public static OState parse(StateDef def) {
        OState oState = new OState();
        oState.setId(def.getId());
        oState.setName(def.getName());
        oState.setDisplayName(def.getDisplayName());
        oState.setCurrency(def.isCurrency());

        oState.setEvent(def.getEvent());
        oState.setDefaultState(def.getDefaultState());
        if (def.getCondition() != null && !def.getCondition().trim().isEmpty()) {
            oState.setCondition(compileExpression(def.getCondition()));
        }
        if (def.getStateValueExpression() != null && ! def.getStateValueExpression().trim().isEmpty()) {
            oState.setStateValueExpression(compileExpression(def.getStateValueExpression()));
        } else {
            throw new IllegalArgumentException("State 'stateValueExpression' has not been set!");
        }

        if (def.getStateChangeAwards() != null) {
            oState.setStateChangeAwards(def.getStateChangeAwards().stream()
                    .map(Parsers::fromDefTo)
                    .collect(Collectors.toList()));
        }

        List<OState.OAState> oaStateList = new ArrayList<>();
        boolean defStateFound = false;
        for (StateDef.State state : def.getStates()) {
            defStateFound = defStateFound || Integer.compare(state.getId(), oState.getDefaultState()) == 0;
            OState.OAState oaState = new OState.OAState();
            oaState.setId(state.getId());
            oaState.setName(state.getName());
            oaState.setPoints(state.getPoints());
            if (state.getCondition() != null && !state.getCondition().trim().isEmpty()) {
                oaState.setCondition(compileExpression(state.getCondition()));
            }
            oaStateList.add(oaState);
        }

        if (!defStateFound) {
            throw new IllegalArgumentException("The default state id is not found within state list in '" +
                    oState.getName() + "'");
        }

        oState.setStates(oaStateList.stream()
                .sorted(Comparator.comparingInt(OState.OAState::getId))
                .collect(Collectors.toList()));
        return oState;
    }

    private static void precheck(PointsAdditional pa, PointDef rule) throws IllegalArgumentException {
        if (pa.getName() == null) {
            throw new IllegalArgumentException("missing field 'id' for one of additionalPoint entry in rule '" + rule.getId() + "'!");
        }
        if (pa.getToUser() == null) {
            throw new IllegalArgumentException("missing field 'toUser' for one of additionalPoint entry in rule '" + rule.getId() + "'!");
        }
    }

    private static Number interpretNumber(Object val, boolean asRealValues) {
        if (Integer.class.isAssignableFrom(val.getClass()) || Long.class.isAssignableFrom(val.getClass())) {
            return asRealValues ? ((Number)val).doubleValue() : ((Number)val).longValue();
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            return ((Number)val).doubleValue();
        } else {
            Double d = strNum(val.toString());
            return asRealValues ? d : d.longValue();
        }
    }

    private static OState.OAStateChangeAwards fromDefTo(StateDef.StateChangeAwards changeAwards) {
        OState.OAStateChangeAwards awards = new OState.OAStateChangeAwards();
        awards.setTo(changeAwards.getTo());
        awards.setFrom(changeAwards.getFrom());
        awards.setPoints(changeAwards.getPoints());
        return awards;
    }

    private static Double strNum(String numStr) {
        Matcher matcher = NUMBER_PATTERN.matcher(numStr);
        if (matcher.find()) {
            double val = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.startsWith("k")) {
                return val * 1000;
            } else if (unit.startsWith("m")) {
                return val * 1000 * 1000;
            } else if (unit.startsWith("b")) {
                return val * 1000 * 1000 * 1000;
            } else {
                return val;
            }
        }
        throw new IllegalArgumentException("Given number is not in the correct format! [" + numStr + "]");
    }

    private static Serializable compileExpression(String expr) {
        return MVEL.compileExpression(expr);
    }

    private static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static boolean isNonEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

}
