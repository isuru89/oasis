package io.github.isuru.oasis.model.events;

/**
 * @author iweerarathna
 */
public final class EventNames {

    public static final String TERMINATE_GAME = "__OASIS_TERMINATE__";

    public static final String OASIS_EVENT_CHALLENGE_WINNER = "oasis.challenge.winner";
    public static final String OASIS_EVENT_RACE_AWARD = "oasis.race.award";

    public static final String OASIS_EVENT_COMPENSATE_POINTS = "oasis.event.point.compensate";
    public static final String OASIS_EVENT_AWARD_BADGE = "oasis.event.badge.manual";
    public static final String OASIS_EVENT_SHOP_ITEM_SHARE = "oasis.event.item.shared";


    public static final String POINT_RULE_COMPENSATION_NAME = OASIS_EVENT_COMPENSATE_POINTS.toLowerCase();
    public static final String POINT_RULE_MILESTONE_BONUS_NAME = "oasis.rules.milestone.bonus";
    public static final String POINT_RULE_BADGE_BONUS_NAME = "oasis.rules.badge.bonus";

    public static final String POINT_RULE_RACE_POINTS = "oasis.rules.point.race";

    public static final String POINT_RULE_CHALLENGE_POINTS = "oasis.rules.point.challenge";

}
