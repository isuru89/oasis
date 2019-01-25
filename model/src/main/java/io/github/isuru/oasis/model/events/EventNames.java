package io.github.isuru.oasis.model.events;

/**
 * @author iweerarathna
 */
public final class EventNames {

    public static final String TERMINATE_GAME = "__OASIS_TERMINATE__";

    public static final String OASIS_CHALLENGE_WINNER = "oasis.challenge.winner";

    public static final String EVENT_COMPENSATE_POINTS = "__OASIS_COMPENSATE_POINTS__";
    public static final String EVENT_AWARD_BADGE = "__OASIS_MANUAL_BADGE__";
    public static final String EVENT_SHOP_ITEM_SHARE = "__OASIS_ITEM_SHARE__";

    public static final String EVENT_RACE_AWARD = "__OASIS_RACE_AWARD__";

    public static final String START_CHALLENGE = "__OASIS_CHALLENGE_START__";
    public static final String CHALLENGE_TIMEOUT = "__OASIS_CHALLENGE_TIMEOUT__";
    public static final String CHALLENGE_COMPLETED = "__OASIS_CHALLENGE_COMPLETED__";


    public static final String POINT_RULE_COMPENSATION_NAME = EVENT_COMPENSATE_POINTS.toLowerCase();
    public static final String POINT_RULE_MILESTONE_BONUS_NAME = "oasis.rules.milestone.bonus";
    public static final String POINT_RULE_BADGE_BONUS_NAME = "oasis.rules.badge.bonus";

    public static final String POINT_RULE_RACE_POINTS = "__oasis_race_award__";

    public static final String POINT_RULE_CHALLENGE_POINTS = "oasis.rules.point.challenge";

}
